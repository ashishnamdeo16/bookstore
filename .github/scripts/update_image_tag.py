#!/usr/bin/env python3
"""Rewrite the image tag for a single service inside a Kubernetes manifest.

Only ``image:`` mapping values whose repository component belongs to the given
service are touched. Everything else in the file -- including env vars such as
``http://book-service:8080`` that happen to contain "<service>:<something>" --
is left byte-for-byte identical.

Exit codes:
    0  manifest updated, or already at the requested tag
    2  usage / precondition failure (missing file, no matching image, bad YAML)
"""

from __future__ import annotations

import argparse
import re
import sys

# Matches a YAML `image:` entry, optionally as a sequence item, optionally
# quoted. The reference itself is captured so it can be parsed properly.
IMAGE_LINE = re.compile(
    r"""^(?P<prefix>\s*(?:-\s+)?image:\s*)
         (?P<quote>["']?)
         (?P<ref>[^"'\s]+)
         (?P=quote)
         (?P<suffix>\s*(?:\#.*)?)$""",
    re.VERBOSE,
)


def fail(message: str) -> "NoReturn":  # type: ignore[valid-type]
    print(f"::error::{message}", file=sys.stderr)
    sys.exit(2)


def split_reference(ref: str) -> tuple[str, str | None]:
    """Split an image reference into (repository, tag-or-digest-part)."""
    if "@" in ref:  # registry/repo[:tag]@sha256:...
        repo = ref.split("@", 1)[0]
        return repo.rsplit(":", 1)[0] if ":" in repo.rsplit("/", 1)[-1] else repo, None
    head, sep, tail = ref.rpartition(":")
    # A colon inside the last path segment is a tag; otherwise it is a registry
    # port (e.g. "localhost:5000/foo") and the reference carries no tag.
    if sep and "/" not in tail:
        return head, tail
    return ref, None


def owns(repository: str, service: str) -> bool:
    return repository == service or repository.endswith("/" + service)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", required=True)
    parser.add_argument("--service", required=True)
    parser.add_argument("--tag", required=True)
    args = parser.parse_args()

    if not args.tag.strip() or not re.fullmatch(r"[A-Za-z0-9_][A-Za-z0-9._-]{0,127}", args.tag):
        fail(f"refusing to write invalid image tag {args.tag!r} for {args.service}")

    try:
        with open(args.manifest, encoding="utf-8") as handle:
            original = handle.read()
    except FileNotFoundError:
        fail(f"manifest {args.manifest} not found in the infra repo")

    lines = original.splitlines(keepends=True)
    matched = 0

    for index, line in enumerate(lines):
        stripped = line.rstrip("\n\r")
        newline = line[len(stripped):]
        match = IMAGE_LINE.match(stripped)
        if not match:
            continue

        repository, current = split_reference(match["ref"])
        if not owns(repository, args.service):
            continue

        matched += 1
        if current == args.tag:
            continue
        lines[index] = (
            f"{match['prefix']}{match['quote']}{repository}:{args.tag}"
            f"{match['quote']}{match['suffix']}{newline}"
        )
        print(f"{args.manifest}: {repository}:{current} -> {repository}:{args.tag}")

    if matched == 0:
        fail(
            f"no image reference for '{args.service}' found in {args.manifest}; "
            "the manifest path or image repository name is wrong"
        )

    updated = "".join(lines)
    if updated == original:
        print(f"{args.manifest}: already at {args.tag}, nothing to do")
        return 0

    validate(updated, args)

    with open(args.manifest, "w", encoding="utf-8") as handle:
        handle.write(updated)
    return 0


def validate(updated: str, args: argparse.Namespace) -> None:
    """Confirm the rewritten text is still parseable Kubernetes YAML."""
    try:
        import yaml  # noqa: PLC0415 - optional, defence in depth
    except ImportError:
        print("::warning::PyYAML unavailable, skipping structural validation")
        return

    try:
        documents = [doc for doc in yaml.safe_load_all(updated) if doc]
    except yaml.YAMLError as error:
        fail(f"rewriting {args.manifest} produced invalid YAML: {error}")

    for document in documents:
        if not isinstance(document, dict) or "kind" not in document:
            fail(f"{args.manifest} contains a document without a 'kind'")

    for image in collect_images(documents):
        repository, tag = split_reference(image)
        if owns(repository, args.service) and tag != args.tag:
            fail(f"{args.manifest} still references {image} after the rewrite")


def collect_images(node: object) -> "list[str]":
    found: list[str] = []
    if isinstance(node, dict):
        for key, value in node.items():
            if key == "image" and isinstance(value, str):
                found.append(value)
            else:
                found.extend(collect_images(value))
    elif isinstance(node, list):
        for item in node:
            found.extend(collect_images(item))
    return found


if __name__ == "__main__":
    sys.exit(main())
