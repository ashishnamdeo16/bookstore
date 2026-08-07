import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

const sidebars: SidebarsConfig = {
  docsSidebar: [
    'intro/overview',
    'getting-started/setup',
    'architecture/overall',
    {
      type: 'category',
      label: 'Services',
      items: [
        'services/api-gateway',
        'services/auth-service',
        'services/user-service',
        'services/book-service',
        'services/order-service',
        'services/payment-service',
        'services/notification-service',
        'services/analytics-service',
      ],
    },
    {
      type: 'category',
      label: 'API Reference',
      items: [
        'api/overview',
        'api/auth',
        'api/users',
        'api/catalog',
        'api/orders',
        'api/payments',
        'api/analytics',
      ],
    },
    'kafka/overview',
    'database/overview',
    'security/overview',
    'frontend/overview',
    {
      type: 'category',
      label: 'Deployment',
      items: [
        'deployment/overview',
        'deployment/ci-cd',
        'deployment/kubernetes',
      ],
    },
    {
      type: 'category',
      label: 'AWS Architecture',
      items: [
        'aws/overview',
      ],
    },
    'monitoring/logging',
    'future/improvements',
  ],

  apiSidebar: [
    {
      type: 'category',
      label: 'API Reference',
      collapsed: false,
      items: [
        'api/overview',
        'api/auth',
        'api/users',
        'api/catalog',
        'api/orders',
        'api/payments',
        'api/analytics',
      ],
    },
  ],
};

export default sidebars;
