<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row>
				<div class="mb8" style="width: 100%">
					<el-button type="primary" class="ml10" v-auth="'wm_delivery_rider'" @click="onTodo('骑手信息')">骑手信息</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_delivery_view'" @click="onTodo('配送查询')">配送查询</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_delivery_accept'" @click="onTodo('骑手接单')">骑手接单</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_delivery_complete'" @click="onTodo('配送完成')">配送完成</el-button>
				</div>
			</el-row>
			<el-empty description="配送中心页面骨架已创建，后续可接入配送单列表与动作" />
		</div>
	</div>
</template>

<script setup lang="ts" name="takeawayDeliveryIndex">
import { useMessage } from '/@/hooks/message';

const onTodo = (action: string) => {
	useMessage().info(`${action}功能待实现`);
};
</script>
