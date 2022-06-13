
import { RouteComponentProps } from 'react-router';

export type UrlProps<T extends string> = { [K in T]: string|null };

const pathnameToQueryStringMap: Map<string, string> = new Map();

beforeEach(() => pathnameToQueryStringMap.clear());

export function setTenantIdInUrl(props: RouteComponentProps, tenantId: number) {
  const endOfTenantId = props.location.pathname.indexOf('/', 1);
  if (endOfTenantId !== -1) {
    props.history.push(`/${tenantId}${props.location.pathname
      .slice(endOfTenantId)}`);
    // eslint-disable-next-line no-param-reassign
    props.location.pathname = `/${tenantId}${props.location.pathname
      .slice(endOfTenantId)}`;
  }
  // Else, the page is not specific to a tenant, so we do nothing
}

export function getPropsFromUrl<T extends UrlProps<string> >(props: RouteComponentProps, defaultValues: T): T {
  const out: { [index: string]: string|null } = { ...defaultValues };
  if (pathnameToQueryStringMap.has(props.location.pathname)) {
    const searchParams = new URLSearchParams(pathnameToQueryStringMap.get(props.location.pathname));
    // eslint-disable-next-line no-return-assign
    searchParams.forEach((value, key) => out[key] = value);
    setPropsInUrl(props, out as T);
  } else {
    const searchParams = new URLSearchParams(props.location.search);
    // eslint-disable-next-line no-return-assign
    searchParams.forEach((value, key) => out[key] = value);
  }
  return out as T;
}

export function setPropsInUrl<T extends UrlProps<string> >(props: RouteComponentProps, urlProps: Partial<T>) {
  const searchParams = new URLSearchParams(pathnameToQueryStringMap.has(props.location.pathname)
    ? pathnameToQueryStringMap.get(props.location.pathname) : props.location.search);

  Object.keys(urlProps).forEach((key) => {
    const value = urlProps[key] as string|null|undefined;
    if (value !== undefined) {
      if (value !== null && value.length > 0) {
        searchParams.set(key, value);
      } else {
        searchParams.delete(key);
      }
    }
  });
  pathnameToQueryStringMap.set(props.location.pathname, `?${searchParams.toString()}`);
  props.history.push(`${props.location.pathname}?${searchParams.toString()}`);
}
