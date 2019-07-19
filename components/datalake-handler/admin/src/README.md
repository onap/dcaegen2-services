# DataLake Admin UI

DataLake Admin UI aims to provide the administrator with a user-friendly dashboard to easily monitor and manage ONAP topics, database, and tools via REST API from DataLake Feeder backend system.

See more [DataLake Proposal on ONAP](https://wiki.onap.org/display/DW/DataLake "DataLake Proposal on ONAP")

### Getting Started

1. Go to project folder and install dependencies:

```bash
npm install
```

2. Launch development server, and open `localhost:4200` in your browser:

```bash
npm start
```

### Project Structure

```
-- app
   -- shared           -----> container of pubilc parts
       -- components          -----> container of pubilc components
           -- alert
           -- toastr-notification
           -- ...more components
       -- modules             -----> container of public business modules
           -- card
           -- modal
           -- search
           -- table
       -- layout              -----> container of basic layout of all pages
           -- header
           -- sidebar
       -- utils               -----> container of general functions
   -- core             -----> container of core functions
       -- models
       -- services
   -- views            -----> container of all business pages
       -- about
       -- dashboard-setting
       -- database
       -- feeder
       -- topics
       -- ...more modules
   -- app-routing.module.ts     -----> container of all pages routers
   -- app-component.css
   -- app-component.html
   -- app-component.ts
   -- app-module.ts
-- assets
   -- i18n
-- index.html
-- main.ts
-- styles.css
-- ...other config documents
```

> The layout is used for organizing a new module, **PLEASE** take care of the difference between `components` and `modules`. Make sure that all parts added in the `components` are `dump components` and all the parts which are related to the **business** should be added in the `modules`. Good luck 🙂.

> The service folder is used for created some reused services. **PLEASE** put the specific services into the related folders. For example, put the toastr-notification service into the `components/toastr-notification` folder.
