import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  title: 'Bookstore Docs',
  tagline: 'Engineering documentation for the Bookstore microservices platform',
  favicon: 'img/favicon.svg',

  future: {
    v4: true,
  },

  url: 'https://bookstore-docs.crazym416.com/',
  baseUrl: '/',

  organizationName: 'ashishnamdeo16',
  projectName: 'bookstore',

  onBrokenLinks: 'throw',

  markdown: {
    mermaid: true,
  },

  themes: ['@docusaurus/theme-mermaid'],

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.ts',
          routeBasePath: 'docs',
          editUrl: 'https://github.com/ashishnamdeo16/bookstore/tree/main/bookstore-docs/',
          showLastUpdateTime: false,
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  plugins: [
    [
      require.resolve('@easyops-cn/docusaurus-search-local'),
      {
        hashed: true,
        language: ['en'],
        indexBlog: false,
        docsRouteBasePath: '/docs',
        highlightSearchTermsOnTargetPage: true,
      },
    ],
  ],

  themeConfig: {
    image: 'img/logo.svg',
    colorMode: {
      defaultMode: 'light',
      disableSwitch: false,
      respectPrefersColorScheme: true,
    },
    docs: {
      sidebar: {
        hideable: true,
        autoCollapseCategories: true,
      },
    },
    navbar: {
      title: 'Bookstore',
      logo: {
        alt: 'Bookstore',
        src: 'img/logo.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docsSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          type: 'docSidebar',
          sidebarId: 'apiSidebar',
          position: 'left',
          label: 'API Reference',
        },
        {
          href: 'https://github.com/ashishnamdeo16/bookstore',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {label: 'Introduction', to: '/docs/intro/overview'},
            {label: 'Getting Started', to: '/docs/getting-started/setup'},
            {label: 'Architecture', to: '/docs/architecture/overall'},
          ],
        },
        {
          title: 'Reference',
          items: [
            {label: 'API Reference', to: '/docs/api/overview'},
            {label: 'Kafka', to: '/docs/kafka/overview'},
            {label: 'Security', to: '/docs/security/overview'},
          ],
        },
        {
          title: 'Project',
          items: [
            {
              label: 'GitHub Repository',
              href: 'https://github.com/ashishnamdeo16/bookstore',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} Bookstore.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'bash', 'json', 'yaml', 'sql', 'docker', 'typescript'],
    },
    mermaid: {
      theme: {light: 'neutral', dark: 'dark'},
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
