import { getAnswers, postAnswers, putAnswers, deleteAnswers } from 'store/rest/RestTestUtils';

export const mockGet = jest.fn().mockImplementation((url) => {
  const response = getAnswers.get(url);
  if (response instanceof Error) {
    return Promise.reject(response);
  }
  return Promise.resolve(response);
});

export const mockPost = jest.fn().mockImplementation((url, params) => {
  const response = postAnswers.get(url + JSON.stringify(params));
  if (response instanceof Error) {
    return Promise.reject(response);
  }

  return Promise.resolve(response);
});

export const mockPut = jest.fn().mockImplementation((url, params) => {
  const response = putAnswers.get(url + JSON.stringify(params));
  if (response instanceof Error) {
    return Promise.reject(response);
  }

  return Promise.resolve(response);
});

export const mockDelete = jest.fn().mockImplementation((url) => {
  const response = deleteAnswers.get(url);
  if (response instanceof Error) {
    return Promise.reject(response);
  }
  return Promise.resolve(response);
});

export const mockUploadFile = jest.fn().mockImplementation((url, file) => {
  const response = postAnswers.get(url + JSON.stringify(file));
  if (response instanceof Error) {
    return Promise.reject(response);
  }
  return Promise.resolve(response);
});

const mock = jest.fn().mockImplementation(() => ({
  get: mockGet,
  post: mockPost,
  put: mockPut,
  delete: mockDelete,
  uploadFile: mockUploadFile,
}));

export default mock;
