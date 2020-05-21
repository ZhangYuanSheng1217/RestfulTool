# RestfulTool

> - `最小版本`: `201`
>   * 低版本请移步[RESTFulToolkit](https://plugins.jetbrains.com/plugin/10292-restfultoolkit) 安装使用
> - `插件地址`: [`RestfulTool`](https://plugins.jetbrains.com/plugin/14280-restfultool)

> + [Github](https://github.com/ZhangYuanSheng1217/RestfulTool)
> + [Gitee](https://gitee.com/zys981029/RestfulTool)

#### 前言
    由于RESTFulToolkit插件原作者不更新了，IDEA.201及以上版本不再适配，所以参照原作者的插件项目制作了此插件。
    欢迎各位提交 issue | pr

#### 介绍
> 一套 Restful 服务开发辅助工具集。
>> 1. 提供了一个 Services tree 的显示窗口;
>> 2. 点击 URL 直接跳转到对应的方法定义;
>> 3. 一个简单的 http 请求工具;
>> 4. 支持 Spring 体系 (Spring MVC / Spring Boot);
>> 5. 支持 `Navigate -> Request Service` 搜索 Mapping `Ctrl + Alt + /`;

#### 安装
> 1. IDEA plugin 搜索`RestfulTool`安装 (推荐)
> 2. 从 [Jetbrains Plugins](https://plugins.jetbrains.com/plugin/14280-restfultool/versions) 仓库下载安装包
> 3. 下载项目根目录下的`RestfulTool.zip`本地安装

#### 使用
> * 搜索
>   - `navigation(导航)` > `Request Service`
>   - 快捷键
>       - 默认：`Ctrl + Alt + /`
>       - 更换：`Setting` > `keymap` > `Plug-ins` > `RestfulTool`
> * 视图
>   - `right tool window(右侧工具栏)` > `RestfulTool`

****
#### Fork
> - 如果运行出现问题，请在Platform SDK中增加以下lib
>   - `.\plugins\Spring\lib\spring.jar`
>   - `.\plugins\properties\lib\properties.jar`
>   - `.\plugins\yaml\lib\yaml.jar`

#### TODO
> + 符合条件才显示工具界面（IDEA扫描完毕）

#### 参考
> + 插件地址 - [RESTFullToolkit](https://plugins.jetbrains.com/plugin/10292-restfultoolkit/)
> + Github - [RESTFullToolkit](https://github.com/mrmanzhaow/RestfulToolkit)
