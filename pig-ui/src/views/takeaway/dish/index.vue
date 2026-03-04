<template>
	<div class="layout-padding">
		<div class="layout-padding-auto layout-padding-view">
			<el-row>
				<div class="mb8" style="width: 100%">
					<el-button type="primary" class="ml10" v-auth="'wm_dish_view'" @click="onTodo('菜品查询')">菜品查询</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_dish_add'" @click="onTodo('菜品新增')">菜品新增</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_dish_edit'" @click="onTodo('菜品编辑')">菜品编辑</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_dish_del'" @click="onTodo('菜品删除')">菜品删除</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_dish_sale_on'" @click="onTodo('菜品上架')">菜品上架</el-button>
					<el-button type="primary" class="ml10" v-auth="'wm_dish_sale_off'" @click="onTodo('菜品下架')">菜品下架</el-button>
				</div>
			</el-row>
			<el-empty description="菜品管理页面骨架已创建，后续可接入分页与编辑能力" />
		</div>
	</div>
</template>

<script setup lang="ts" name="takeawayDishIndex">
import { useMessage } from '/@/hooks/message';

const onTodo = (action: string) => {
	useMessage().info(`${action}功能待实现`);
};
</script>
