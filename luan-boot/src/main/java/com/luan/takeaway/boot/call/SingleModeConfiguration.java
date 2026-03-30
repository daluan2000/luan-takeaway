package com.luan.takeaway.boot.call;

import com.luan.takeaway.takeaway.common.call.DishServiceCallFacade;
import com.luan.takeaway.takeaway.common.call.DeliveryServiceCallFacade;
import com.luan.takeaway.takeaway.common.call.OrderServiceCallFacade;
import org.springframework.context.annotation.Configuration;

/**
 * 单体模式配置
 * <p>
 * 当配置 {@code takeaway.deploy-mode=single} 时启用，使用本地调用。
 * 由于 Local 实现类已使用 @Component + @Primary，无需额外配置。
 *
 * @author luan
 */
@Configuration
public class SingleModeConfiguration {

}
