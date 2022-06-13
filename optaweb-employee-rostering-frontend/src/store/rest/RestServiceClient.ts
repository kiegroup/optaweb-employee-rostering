
import { AxiosInstance, AxiosResponse, AxiosStatic } from 'axios';

import { alert } from 'store/alert';
import { ServerSideExceptionInfo, BasicObject } from 'types';
import { ThunkDispatch } from 'redux-thunk';
import { AppState } from 'store/types';
import { setConnectionStatus } from 'store/tenant/actions';
import { refreshTenantList } from 'store/tenant/operations';

const typeJsonRegex = new RegExp('application/json.*');
export default class RestServiceClient {
  restClient: AxiosInstance;

  pollForServerTimeout: number | null;

  dispatch: ThunkDispatch<AppState, any, any> | null;

  constructor(baseURL: string, axios: AxiosStatic) {
    this.restClient = axios.create({
      baseURL,
      validateStatus: () => true,
    });
    this.dispatch = null;
    this.pollForServerTimeout = null;
    this.handleResponse = this.handleResponse.bind(this);
  }

  setDispatch(dispatch: ThunkDispatch<AppState, any, any>) {
    this.dispatch = dispatch;
  }

  get<T>(url: string): Promise<T> {
    return this.restClient.get<T>(url).then(this.handleResponse).catch((err) => {
      this.handleError(err);
      throw err;
    });
  }

  post<T>(url: string, params: any): Promise<T> {
    return this.restClient.post<T>(url, params).then(this.handleResponse).catch((err) => {
      this.handleError(err);
      throw err;
    });
  }

  uploadFile<T>(url: string, file: File): Promise<T> {
    const data = new FormData();
    data.append('file', file);
    return this.restClient.post<T>(url, data).then(this.handleResponse);
  }

  put<T>(url: string, params: any): Promise<T> {
    return this.restClient.put<T>(url, params).then(this.handleResponse).catch((err) => {
      this.handleError(err);
      throw err;
    });
  }

  delete<T>(url: string): Promise<T> {
    return this.restClient.delete<T>(url)
      .then(this.handleResponse).catch((err) => {
        this.handleError(err);
        throw err;
      });
  }

  handleResponse<T>(res: AxiosResponse<T>): Promise<T> {
    if (res.status >= 200 && res.status < 300) {
      if (this.dispatch) {
        this.dispatch(setConnectionStatus(true));
      }
      return Promise.resolve(res.data);
    }
    if (res.status >= 502 && res.status <= 504) { // 502-504 are gateway related responses
      this.handleError({
        isAxiosError: true,
      });
      return Promise.reject(res.status);
    }
    if (this.dispatch !== null) {
      if (typeJsonRegex.test(res.headers['content-type'])) {
        this.dispatch(alert.showServerError(res.data as unknown as ServerSideExceptionInfo & BasicObject));
      } else {
        this.dispatch(alert.showServerErrorMessage(res.statusText));
      }
      return Promise.reject(res.status);
    }

    throw Error('Dispatch was not passed to RestServiceClient');
  }

  handleError(err: { isAxiosError: boolean }): void {
    if (err.isAxiosError && this.pollForServerTimeout === null) {
      if (this.dispatch) {
        this.dispatch(setConnectionStatus(false));
      }
      this.pollForServerTimeout = window.setTimeout(() => {
        this.pollForServerTimeout = null;
        if (this.dispatch !== null) {
          this.dispatch(refreshTenantList());
        }
      }, 1000);
    }
  }
}
