<template>
	<div class="layout-navbars-breadcrumb-user-news">
		<div class="head-box">
			<div class="head-box-title">{{ $t('user.newTitle') }}</div>
			<div class="head-box-btn" v-if="newsList.length > 0" @click="onAllReadClick">{{ $t('user.newBtn') }}</div>
		</div>
		<div class="content-box">
			<template v-if="newsList.length > 0">
				<div class="content-box-item" v-for="(v, k) in newsList" :key="k">
					<div class="content-box-title">{{ v.title }}</div>
					<div class="content-box-msg">
						{{ v.content }}
					</div>
					<div class="content-box-time">{{ v.time }}</div>
				</div>
			</template>
			<el-empty :description="$t('user.newDesc')" v-else></el-empty>
		</div>
	</div>
</template>

<script setup lang="ts" name="layoutBreadcrumbUserNews">
// 定义变量内容
import { useMsg } from '/@/stores/msg';

interface WsPayload {
	title?: string;
	content?: string;
	timestamp?: string | number;
}

interface NewsItem {
	title: string;
	content: string;
	time: string;
}

const formatNewsTime = (value: string | number | undefined, fallback: string) => {
	if (value === undefined || value === null || value === '') {
		return fallback;
	}
	const timeNum = Number(value);
	if (!Number.isFinite(timeNum)) {
		return fallback;
	}
	const date = new Date(timeNum);
	if (Number.isNaN(date.getTime())) {
		return fallback;
	}
	const pad = (num: number) => `${num}`.padStart(2, '0');
	return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
};

const normalizeNewsItem = (raw: any): NewsItem => {
	const fallbackTitle = raw?.label || '通知';
	const fallbackContent = typeof raw?.value === 'string' ? raw.value : String(raw?.value ?? '');
	const fallbackTime = raw?.time || '';

	if (typeof raw?.value !== 'string') {
		return { title: fallbackTitle, content: fallbackContent, time: fallbackTime };
	}

	const text = raw.value.trim();
	if (!text.startsWith('{')) {
		return { title: fallbackTitle, content: text || fallbackContent, time: fallbackTime };
	}

	try {
		const payload = JSON.parse(text) as WsPayload;
		return {
			title: payload.title?.trim() || fallbackTitle,
			content: payload.content?.trim() || fallbackContent,
			time: formatNewsTime(payload.timestamp, fallbackTime),
		};
	}
	catch {
		return { title: fallbackTitle, content: text || fallbackContent, time: fallbackTime };
	}
};

const newsList = computed(() => {
	return useMsg()
		.getAllMsg()
		.map((item: any) => normalizeNewsItem(item));
});

// 全部已读点击
const onAllReadClick = () => {
	useMsg().removeAll();
};
</script>

<style scoped lang="scss">
.layout-navbars-breadcrumb-user-news {
	.head-box {
		display: flex;
		border-bottom: 1px solid var(--el-border-color-lighter);
		box-sizing: border-box;
		color: var(--el-text-color-primary);
		justify-content: space-between;
		height: 35px;
		align-items: center;

		.head-box-btn {
			color: var(--el-color-primary);
			font-size: 13px;
			cursor: pointer;
			opacity: 0.8;

			&:hover {
				opacity: 1;
			}
		}
	}

	.content-box {
		font-size: 13px;

		.content-box-item {
			padding-top: 12px;

			.content-box-title {
				font-weight: 600;
			}

			&:last-of-type {
				padding-bottom: 12px;
			}

			.content-box-msg {
				color: var(--el-text-color-secondary);
				margin-top: 5px;
				margin-bottom: 5px;
			}

			.content-box-time {
				color: var(--el-text-color-secondary);
			}
		}
	}

	.foot-box {
		height: 35px;
		color: var(--el-color-primary);
		font-size: 13px;
		cursor: pointer;
		opacity: 0.8;
		display: flex;
		align-items: center;
		justify-content: center;
		border-top: 1px solid var(--el-border-color-lighter);

		&:hover {
			opacity: 1;
		}
	}

	:deep(.el-empty__description p) {
		font-size: 13px;
	}
}
</style>
