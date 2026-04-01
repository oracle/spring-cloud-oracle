import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

// This runs in Node.js - Don't use client-side code here (browser APIs, JSX...)

const config: Config = {
  title: 'Spring Cloud Oracle',
  tagline: 'Integrate Spring Boot, OCI, and Oracle AI Database',
  favicon: 'img/favicon-32x32.png',

  // Future flags, see https://docusaurus.io/docs/api/docusaurus-config#future
  future: {
    v4: true, // Improve compatibility with the upcoming Docusaurus v4
  },

  // TODO: Set the production url of your site here
  url: 'https://oracle.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/spring-cloud-oracle/docs/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'oracle', // Usually your GitHub org/user name.
  projectName: 'spring-cloud-oracle', // Usually your repo name.

  onBrokenLinks: 'throw',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
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
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
            'https://github.com/oracle/spring-cloud-oracle/tree/main/site/',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/logo.png',
    navbar: {
      title: 'Spring Cloud Oracle',
      logo: {
        alt: 'Spring Cloud Oracle Logo',
        src: 'img/spring_cloud.png',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: 'Docs',
        },
        {
          type: 'docsVersionDropdown',
          versions: ['current', '2.0.0' ]
        },
        {
          href: 'https://github.com/oracle/spring-cloud-oracle',
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
            {
              label: 'Tutorial',
              to: '/docs/intro',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'Stack Overflow',
              href: 'https://stackoverflow.com/questions/tagged/oracle',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'Oracle Blogs',
              to: 'https://blogs.oracle.com/',
            },
            {
              label: 'Oracle LiveLabs',
              to: 'https://livelabs.oracle.com/pls/apex/r/dbpm/livelabs/home'
            },
            {
              label: 'GitHub',
              href: 'https://github.com/oracle/spring-cloud-oracle',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()}, Oracle and/or its affiliates. Built with ❤️ using Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
