/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as alerts from "ui/Alerts";
import RestServiceClient from "./RestServiceClient";
import { AxiosStatic } from "axios";

const mockGet = jest.fn();
const mockPost = jest.fn();
const mockPut = jest.fn();
const mockDelete = jest.fn();
const mockCreate = jest.fn(() => ({
  get: mockGet,
  post: mockPost,
  put: mockPut,
  delete: mockDelete
}));

const axois: AxiosStatic = {
  create: mockCreate
} as any;

beforeEach(() => {
  mockGet.mockClear();
  mockPost.mockClear();
  mockPut.mockClear();
  mockDelete.mockClear();
  mockCreate.mockClear();
});

describe("Rest Service Client", () => {
  it("Should call axois constructor with correct arguments", () => {
    const baseURL = "/rest";
    const restServiceClient = new RestServiceClient(baseURL, axois);
    expect(mockCreate).toBeCalled();
    expect(mockCreate).toBeCalledWith({
      baseURL: baseURL,
      validateStatus: expect.any(Function)
    });
    expect(restServiceClient).toEqual(expect.any(RestServiceClient));
  });

  it("Should call axios.get on get", async () => {
    const baseURL = "/rest";
    const restServiceClient = new RestServiceClient(baseURL, axois);
    const handleResponseSpy = jest.spyOn(restServiceClient, "handleResponse");
    const targetURL = "/endpoint";
    const response = {
      status: 200,
      data: {},
      statusText: "Ok",
      headers: {},
      config: {}
    };
    mockGet.mockReturnValue(Promise.resolve(response));
    await restServiceClient.get(targetURL);

    expect(mockGet).toBeCalled();
    expect(mockGet).toBeCalledWith("/endpoint");
    expect(handleResponseSpy).toBeCalledWith(response);
  });

  it("Should call axios.post on post", async () => {
    const baseURL = "/rest";
    const restServiceClient = new RestServiceClient(baseURL, axois);
    const handleResponseSpy = jest.spyOn(restServiceClient, "handleResponse");
    const targetURL = "/endpoint";
    const data = {
      a: "Hello",
      b: 2
    };
    const response = {
      status: 200,
      data: {},
      statusText: "Ok",
      headers: {},
      config: {}
    };
    mockPost.mockReturnValue(Promise.resolve(response));
    await restServiceClient.post(targetURL, data);

    expect(mockPost).toBeCalled();
    expect(mockPost).toBeCalledWith("/endpoint", data);
    expect(handleResponseSpy).toBeCalledWith(response);
  });

  it("Should call axios.put on put", async () => {
    const baseURL = "/rest";
    const restServiceClient = new RestServiceClient(baseURL, axois);
    const handleResponseSpy = jest.spyOn(restServiceClient, "handleResponse");
    const targetURL = "/endpoint";
    const data = {
      a: "Hello",
      b: 2
    };
    const response = {
      status: 200,
      data: {},
      statusText: "Ok",
      headers: {},
      config: {}
    };
    mockPut.mockReturnValue(Promise.resolve(response));
    await restServiceClient.put(targetURL, data);

    expect(mockPut).toBeCalled();
    expect(mockPut).toBeCalledWith("/endpoint", data);
    expect(handleResponseSpy).toBeCalledWith(response);
  });

  it("Should call axios.delete on delete", async () => {
    const baseURL = "/rest";
    const restServiceClient = new RestServiceClient(baseURL, axois);
    const handleResponseSpy = jest.spyOn(restServiceClient, "handleResponse");
    const targetURL = "/endpoint";
    const response = {
      status: 200,
      data: {},
      statusText: "Ok",
      headers: {},
      config: {}
    };
    mockDelete.mockReturnValue(Promise.resolve(response));
    await restServiceClient.delete(targetURL);

    expect(mockDelete).toBeCalled();
    expect(mockDelete).toBeCalledWith("/endpoint");
    expect(handleResponseSpy).toBeCalledWith(response);
  });

  it("Should resolves to the value on success", async () => {
    const baseURL = "/rest";
    const restServiceClient = new RestServiceClient(baseURL, axois);
    const data = {
      a: "Test",
      b: 2
    };

    const response = {
      status: 200,
      data: data,
      statusText: "Ok",
      headers: {},
      config: {}
    };

    expect(restServiceClient.handleResponse(response)).resolves.toEqual(data);
  });

  it("Should reject the promise on failure", async () => {
    const showServerErrorMessageMock = jest.spyOn(alerts, "showServerErrorMessage");
    const baseURL = "/rest";
    const restServiceClient = new RestServiceClient(baseURL, axois);
    const data = {
      a: "Test",
      b: 2
    };
    const errorStatus = "I am a teapot";
    const response = {
      status: 404,
      data: data,
      statusText: errorStatus,
      headers: {},
      config: {}
    };
    await expect(restServiceClient.handleResponse(response)).rejects.toEqual(404);
    expect(showServerErrorMessageMock).toBeCalledWith(errorStatus);
  });
});