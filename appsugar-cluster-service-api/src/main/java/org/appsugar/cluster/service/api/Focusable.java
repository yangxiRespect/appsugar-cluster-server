package org.appsugar.cluster.service.api;

import java.util.List;
import java.util.Set;

/**
 * 关注接口
 * @author NewYoung
 * 2017年5月11日下午1:33:22
 */
public interface Focusable {

	/**
	 * 关注一般普通服务
	 * @author NewYoung
	 * 2017年5月11日下午2:12:44
	 */
	public void focusNormalService(String name);

	/**
	 * 关注动态服务
	 * @author NewYoung
	 * 2017年5月11日下午2:12:55
	 */
	public void focusDynamicService(String name, String sequence);

	/**
	 * 关注特殊服务(动态创建服务会关注所有动态服务)
	 * @author NewYoung
	 * 2017年5月11日下午2:13:01
	 */
	public void focusSpecial(String name);

	/**
	 * 返回所有关注的一般服务
	 * @author NewYoung
	 * 2017年5月11日下午2:15:05
	 */
	public Set<String> normalFocus();

	/**
	 * 返回所有关注的动态服务
	 * @author NewYoung
	 * 2017年5月11日下午2:15:47
	 */
	public Set<String> dynamicFocus();

	/**
	 * 返回所有关注的特殊服务
	 * @author NewYoung
	 * 2017年5月11日下午2:15:55
	 */
	public Set<String> specialFocus();

	/**
	 * 返回我所提供的所有服务
	 * @author NewYoung
	 * 2017年5月26日下午3:37:09
	 */
	public Set<String> supplys();

	/**返回我所见节点**/
	public Set<String> memberSet();

	/**返回节点所提供服务**/
	public List<String> memberServices();
}
