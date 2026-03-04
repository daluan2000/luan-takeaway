<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row>
				<div class="mb8" style="width: 100%">
					<el-button type="primary" class="ml10" v-auth="'wm_merchant_view'" @click="onTodo('商家查询')">商家查询</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_merchant_apply'" @click="onTodo('商家入驻')">商家入驻</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_merchant_audit'" @click="onTodo('商家审核')">商家审核</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_merchant_edit'" @click="onTodo('商家编辑')">商家编辑</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_merchant_business'" @click="onTodo('营业状态')">营业状态</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_merchant_accept'" @click="onTodo('商家接单')">商家接单</el-button>
				</div>
			</el-row>
			<el-empty description="商家中心页面骨架已创建，后续可接入列表和表单接口" />
		</div>
	</div>
</template>

<script setup lang="ts" name="takeawayMerchantIndex">
import { useMessage } from '/@/hooks/message';

const onTodo = (action: string) => {
	useMessage().info(`${action}功能待实现`);
};
</script>
