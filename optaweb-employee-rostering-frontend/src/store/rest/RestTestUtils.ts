import RestServiceClient from './RestServiceClient';

export const getAnswers: Map<string, any> = new Map();
export const postAnswers: Map<string, any> = new Map();
export const putAnswers: Map<string, any> = new Map();
export const deleteAnswers: Map<string, any> = new Map();

export function onGet(url: string, answer: any) { getAnswers.set(url, answer); }
export function onPost(url: string, params: any, answer: any) { postAnswers.set(url + JSON.stringify(params), answer); }
export function onUploadFile(url: string, file: any, answer: any) {
  postAnswers.set(url + JSON.stringify(file), answer);
}
export function onPut(url: string, params: any, answer: any) { putAnswers.set(url + JSON.stringify(params), answer); }
export function onDelete(url: string, answer: any) { deleteAnswers.set(url, answer); }

export function resetRestClientMock(mock: jest.Mocked<RestServiceClient>) {
  getAnswers.clear();
  postAnswers.clear();
  putAnswers.clear();
  deleteAnswers.clear();
  mock.get.mockClear();
  mock.post.mockClear();
  mock.put.mockClear();
  mock.delete.mockClear();
}
