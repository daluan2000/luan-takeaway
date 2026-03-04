<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row>
				<div class="mb8" style="width: 100%">
					<el-button type="primary" class="ml10" v-auth="'wm_order_view'" @click="onTodo('订单查询')">订单查询</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_order_create'" @click="onTodo('订单创建')">订单创建</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_order_detail'" @click="onTodo('订单详情')">订单详情</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_order_cancel'" @click="onTodo('订单取消')">订单取消</el-button>
				</div>
			</el-row>
			<el-empty description="订单中心页面骨架已创建，后续可接入订单列表和状态流转" />
		</div>
	</div>
</template>

<script setup lang="ts" name="takeawayOrderIndex">
import { useMessage } from '/@/hooks/message';

const onTodo = (action: string) => {
	useMessage().info(`${action}功能待实现`);
};
</script>
