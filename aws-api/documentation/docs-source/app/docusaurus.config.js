// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require("prism-react-renderer/themes/github");
const darkCodeTheme = require("prism-react-renderer/themes/dracula");

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: "Aws Documentation Site",
  tagline: "Hello, IT. Have you tried turning it off and on again?",
  url: "https://docs.Aws.wipo.int",
  baseUrl: "/",
  onBrokenLinks: "log",
  onBrokenMarkdownLinks: "warn",
  favicon: "img/favicon.ico",
  markdown: {
    mermaid: true,
  },
  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: "WIPO", // Usually your GitHub org/user name.
  projectName: "Aws", // Usually your repo name.

  presets: [
    [
      "classic",
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve("./sidebars.js"),
          docLayoutComponent: "@theme/DocPage",
          docItemComponent: "@theme/ApiItem", // Derived from docusaurus-theme-openapi
        },
        // blog: {
        //   showReadingTime: true,
        //   // Please change this to your repo.
        //   // Remove this to remove the "edit this page" links.
        //   editUrl:
        //     "https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/",
        // },
        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      docs: {
        sidebar: {
          hideable: true,
        },
      },
      navbar: {
        title: "Aws Docs",
        logo: {
          alt: "Aws Logo",
          src: "img/logo.svg",
        },
        items: [
          {
            type: "doc",
            docId: "overview",
            position: "left",
            label: "Docs",
          },
          // { to: "/blog", label: "Blog", position: "left" },
          {
            label: "Aws API",
            position: "left",
            to: "/docs/Awsapi/madrid-object-storage-system-api",
          },
        ],
      },
      footer: {
        style: "dark",
        links: [

        ],
        copyright: `Copyright © ${new Date().getFullYear()} Aws Project, Inc. Built with Docusaurus.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
        additionalLanguages: ["ruby", "csharp", "php"],
      },
    }),

  plugins: [

    [
      "docusaurus-plugin-openapi-docs",
      {
        id: "Awsapi",
        docsPluginId: "classic",
        config: {
          Awsapi: {
            specPath: "Aws-api-v1/openapi.yaml",
            outputDir: "docs/Awsapi",
            sidebarOptions: {
              groupPathsBy: "tag",
              categoryLinkSource: "tag",
            },
          },
          Awsapiv2: {
            specPath: "Aws-api-v2/openapi_v2.yaml",
            outputDir: "docs/Awsapi/v2/",
          },
        },
      },
    ],
  ],

  themes: ["docusaurus-theme-openapi-docs", '@docusaurus/theme-mermaid'],
};

module.exports = config;
