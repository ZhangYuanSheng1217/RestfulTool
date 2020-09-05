# RestfulTool

<em style="color: red">这不是原插件[~~RestfulToolkit~~]()</u></em>

> - `最小版本`: `201`
>   * 低版本请移步[`RESTFulToolkit`](https://plugins.jetbrains.com/plugin/10292-restfultoolkit) 安装使用
> - `插件地址`: [`RestfulTool`](https://plugins.jetbrains.com/plugin/14280-restfultool)

> + [Github](https://github.com/ZhangYuanSheng1217/RestfulTool)
> + [Gitee](https://gitee.com/zys981029/RestfulTool)

> - [English](./README.md)

#### 前言
    由于RESTFulToolkit插件原作者不更新了，IDEA.201及以上版本不再适配，所以参照原作者的插件项目制作了此插件。
    欢迎各位提交 issue | pr
    如果您觉得还不错，麻烦帮我点个start吧(*￣︶￣)

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
> - ![gif](src/main/resources/tips/images/tip.gif)

****
#### Fork
> - 增加适配图标
>   - 将16x16的svg图标文件放入`/icons/method/{图标主题名}`
>   - 图标命名方式（只支持 png | svg 格式）:<br/>
>
>       | FileName | Directions |
>       | :---: | :---: |
>       | `GET.[svg,png]`                    | `GET`方式请求的默认图标          |
>       | `GET_select.[svg,png]`             | `GET`方式请求的选中图标          |
>       | `POST.[svg,png]`                   | `POST`方式请求的默认图标         |
>       | `POST_select.[svg,png]`            | `POST`方式请求的选中图标         |
>       | `${HttpMethod}[_select].[svg,png]` | `HttpMethod`方式请求的(选中)图标 |
>   - 参考`/icons/method/default`或`/icons/method/cute`

#### 参考
> + 插件地址 - [RESTFullToolkit](https://plugins.jetbrains.com/plugin/10292-restfultoolkit/)
> + Github - [RESTFullToolkit](https://github.com/mrmanzhaow/RestfulToolkit)
