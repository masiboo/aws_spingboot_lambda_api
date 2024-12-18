/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.
 */

// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  // By default, Docusaurus generates a sidebar from the docs folder structure
  tutorialSidebar: [{ type: "autogenerated", dirName: "." }],
  openApiSidebar: [
    {
      type: "category",
      label: "Awsapi",
      link: {
        type: "generated-index",
        title: "Aws API",
        description:
          "swagger.",
        slug: "Awsapi/Aws-registry-api-dev-eu-central-1-551493771163"
      },
      // @ts-ignore
      items: require("./docs/Awsapi/sidebar.js")
    }
  ]

  // But you can create a sidebar manually
  /*
  tutorialSidebar: [
    {
      type: 'category',
      label: 'Tutorial',
      items: ['hello'],
    },
  ],
   */
};

module.exports = sidebars;
