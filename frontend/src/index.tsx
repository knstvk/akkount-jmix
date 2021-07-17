import * as React from "react";
import * as ReactDOM from "react-dom";
import App from "./app/App";
// import registerServiceWorker from './registerServiceWorker';
import { CubaAppProvider } from "@cuba-platform/react";

import { HashRouter, Route } from "react-router-dom";
import { initializeApp } from "@haulmont/jmix-rest";
import { JMIX_REST_URL, REST_CLIENT_ID, REST_CLIENT_SECRET } from "./config";

import "antd/dist/antd.min.css";
import "@cuba-platform/react/dist/index.min.css";
import "./index.css";
import { antdLocaleMapping, messagesMapping } from "./i18n/i18nMappings";
import "moment/locale/ru";

import {JmixAppProvider} from "@haulmont/jmix-react-core";
import 'mobx-react-lite/batchingForReactDom';

export const jmixREST = initializeApp({
  name: "akk",
  apiUrl: JMIX_REST_URL,
  restClientId: REST_CLIENT_ID,
  restClientSecret: REST_CLIENT_SECRET,
  storage: window.localStorage,
  defaultLocale: "en"
});

ReactDOM.render(
  <JmixAppProvider jmixREST={jmixREST}>
    messagesMapping={messagesMapping}
    antdLocaleMapping={antdLocaleMapping}
  >
    <HashRouter>
      <Route component={App} />
    </HashRouter>
  </JmixAppProvider>,
  document.getElementById("root") as HTMLElement
);
// registerServiceWorker();
