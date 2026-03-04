<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row>
				<div class="mb8" style="width: 100%">
					<el-button type="primary" class="ml10" v-auth="'wm_pay_view'" @click="onTodo('支付查询')">支付查询</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_pay_mock'" @click="onTodo('模拟支付')">模拟支付</el-button>
				</div>
			</el-row>
			<el-empty description="支付管理页面骨架已创建，后续可接入支付记录查询与触发" />
		</div>
	</div>
</template>

<script setup lang="ts" name="takeawayPayIndex">
import { useMessage } from '/@/hooks/message';

const onTodo = (action: string) => {
	useMessage().info(`${action}功能待实现`);
};
</script>
