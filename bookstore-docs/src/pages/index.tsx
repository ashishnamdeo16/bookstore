import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import Heading from '@theme/Heading';

import styles from './index.module.css';

type Card = {
  title: string;
  description: string;
  to: string;
  icon: ReactNode;
};

type Stat = {label: string; value: string};

const stats: Stat[] = [
  {value: '8', label: 'Microservices'},
  {value: '6', label: 'Databases'},
  {value: '3', label: 'Kafka topics'},
  {value: '1', label: 'API gateway'},
];

const cards: Card[] = [
  {
    title: 'Getting Started',
    description: 'Prerequisites, environment variables, Docker Compose, and local JVM runs.',
    to: '/docs/getting-started/setup',
    icon: <IconRocket />,
  },
  {
    title: 'Architecture',
    description: 'Gateway routing, sync vs async paths, database-per-service, and security.',
    to: '/docs/architecture/overall',
    icon: <IconLayers />,
  },
  {
    title: 'Services',
    description: 'Auth, user, book, order, payment, notification, analytics, and the API gateway.',
    to: '/docs/services/api-gateway',
    icon: <IconGrid />,
  },
  {
    title: 'API Reference',
    description: 'Per-endpoint request/response shapes pulled straight from the controllers.',
    to: '/docs/api/overview',
    icon: <IconBraces />,
  },
  {
    title: 'Kafka',
    description: 'Topics, producers, consumers, and the checkout → payment → order event chain.',
    to: '/docs/kafka/overview',
    icon: <IconStream />,
  },
  {
    title: 'Frontend',
    description: 'React SPA structure, JWT session handling, cart limits, and Stripe Elements.',
    to: '/docs/frontend/overview',
    icon: <IconWindow />,
  },
];

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--bookstore', styles.heroBanner)}>
      <div className="container">
        <span className={styles.eyebrow}>Microservices Platform</span>
        <Heading as="h1" className="hero__title">
          {siteConfig.title}
        </Heading>
        <p className="hero__subtitle">{siteConfig.tagline}</p>
        <div className={styles.buttons}>
          <Link className="button button--primary button--lg" to="/docs/intro/overview">
            Read the docs
          </Link>
          <Link
            className="button button--secondary button--lg"
            to="/docs/getting-started/setup"
            style={{marginLeft: '0.75rem'}}
          >
            Get started
          </Link>
        </div>
        <dl className={styles.stats}>
          {stats.map((s) => (
            <div key={s.label} className={styles.stat}>
              <dt className={styles.statValue}>{s.value}</dt>
              <dd className={styles.statLabel}>{s.label}</dd>
            </div>
          ))}
        </dl>
      </div>
    </header>
  );
}

export default function Home(): ReactNode {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`${siteConfig.title}`}
      description="Official engineering documentation for the Bookstore microservices application."
    >
      <HomepageHeader />
      <main>
        <section className="container" style={{paddingBottom: '3.5rem'}}>
          <div className="home-grid">
            {cards.map((card) => (
              <Link key={card.to} className="home-card" to={card.to}>
                <span className={styles.cardIcon} aria-hidden="true">
                  {card.icon}
                </span>
                <h3>{card.title}</h3>
                <p>{card.description}</p>
              </Link>
            ))}
          </div>
        </section>
      </main>
    </Layout>
  );
}

function svgProps() {
  return {
    width: 22,
    height: 22,
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.8,
    strokeLinecap: 'round' as const,
    strokeLinejoin: 'round' as const,
  };
}

function IconRocket() {
  return (
    <svg {...svgProps()}>
      <path d="M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z" />
      <path d="M12 15l-3-3a22 22 0 0 1 8-10c1.6 1.6 1.6 6.4 0 8a22 22 0 0 1-5 5z" />
      <path d="M9 12H4s.55-3.03 2-4c1.62-1.08 5 0 5 0M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5" />
    </svg>
  );
}
function IconLayers() {
  return (
    <svg {...svgProps()}>
      <path d="M12 2 2 7l10 5 10-5-10-5z" />
      <path d="m2 17 10 5 10-5M2 12l10 5 10-5" />
    </svg>
  );
}
function IconGrid() {
  return (
    <svg {...svgProps()}>
      <rect x="3" y="3" width="7" height="7" rx="1" />
      <rect x="14" y="3" width="7" height="7" rx="1" />
      <rect x="3" y="14" width="7" height="7" rx="1" />
      <rect x="14" y="14" width="7" height="7" rx="1" />
    </svg>
  );
}
function IconBraces() {
  return (
    <svg {...svgProps()}>
      <path d="M8 3H7a2 2 0 0 0-2 2v5a2 2 0 0 1-2 2 2 2 0 0 1 2 2v5a2 2 0 0 0 2 2h1" />
      <path d="M16 3h1a2 2 0 0 1 2 2v5a2 2 0 0 0 2 2 2 2 0 0 0-2 2v5a2 2 0 0 1-2 2h-1" />
    </svg>
  );
}
function IconStream() {
  return (
    <svg {...svgProps()}>
      <circle cx="5" cy="6" r="2" />
      <circle cx="5" cy="18" r="2" />
      <circle cx="19" cy="12" r="2" />
      <path d="M7 6h6a4 4 0 0 1 4 4M7 18h6a4 4 0 0 0 4-4" />
    </svg>
  );
}
function IconWindow() {
  return (
    <svg {...svgProps()}>
      <rect x="3" y="4" width="18" height="16" rx="2" />
      <path d="M3 9h18M7 6.5h.01M10 6.5h.01" />
    </svg>
  );
}
