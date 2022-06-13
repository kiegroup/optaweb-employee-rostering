import '@patternfly/react-core/dist/styles/base.css';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { SpinnerIcon } from '@patternfly/react-icons';
import './index.css';
import { I18nextProvider } from 'react-i18next';
import { configureStore } from 'store';
import { List } from 'immutable';
import App from './ui/App';

// import i18n (needs to be bundled)
import i18n from './i18n';

const path = window.location.pathname;
let windowTenantId = 0;
if (path.indexOf('/', 1) > 0) {
  windowTenantId = parseInt(path.substring(1, path.indexOf('/', 1)), 10);
}

const store = configureStore({
  restBaseURL: `${process.env.REACT_APP_BACKEND_URL}/rest`,
}, {
  tenantData: {
    currentTenantId: windowTenantId,
    tenantList: List(),
    timezoneList: [],
  },
});

const LoadingSpinner: React.FC = () => (
  <>
    <SpinnerIcon />
    Loading...
  </>
);

ReactDOM.render(
  <I18nextProvider i18n={i18n}>
    <Provider store={store}>
      <BrowserRouter>
        <React.Suspense fallback={<LoadingSpinner />}>
          <App />
        </React.Suspense>
      </BrowserRouter>
    </Provider>
  </I18nextProvider>,
  document.getElementById('root') as HTMLElement,
);
