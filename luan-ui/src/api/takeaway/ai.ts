import request from '/@/utils/request';

export interface AiAssistantRequestPayload {
	message: string;
	merchantUserId?: number;
	limit?: number;
}

export function recommendAiOrder(data: AiAssistantRequestPayload) {
	return request({
		url: '/takeaway/ai/assistant/recommend',
		method: 'post',
		data,
	});
}
