# 2.0所要实现的需求

- 由于部分项目解析获取解析器会报错，该部分改为自己静态解析
- 链式调用，顺序会与实际调用相反
- 配置调整为yml配置

## 开发参考

[XMind API](https://github.com/xmindltd/xmind/wiki/UsingXmindAPI)

[颜色设置参考](https://github.com/xmindltd/xmind/wiki/UsingXmindAPI#setting-the-style-or-making-topics-look-pretty)

旧版本没有`Styles`这个类，找下代码

[Styles.java#L161](https://github.com/xmindltd/xmind/blob/master/bundles/org.xmind.ui.mindmap/src/org/xmind/ui/style/Styles.java#L161)

[DOMConstants.java#L45](https://github.com/xmindltd/xmind/blob/master/bundles/org.xmind.core/src/org/xmind/core/internal/dom/DOMConstants.java#L45)

可以在 xmind 文件中设置好保存，然后用VSCode打开，搜索节点，看属性怎么设置的


## 代码扫描 SonarCloud

指标  | 徽章
---   | ---
安全  | [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=styzf_java-to-diagrams&metric=security_rating)](https://sonarcloud.io/dashboard?id=styzf_java-to-diagrams)
可维护| [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=styzf_java-to-diagrams&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=styzf_java-to-diagrams)
可靠性| [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=styzf_java-to-diagrams&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=styzf_java-to-diagrams)
错误  | [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=styzf_java-to-diagrams&metric=bugs)](https://sonarcloud.io/dashboard?id=styzf_java-to-diagrams)
漏洞  | [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=styzf_java-to-diagrams&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=styzf_java-to-diagrams)
代码行| [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=styzf_java-to-diagrams&metric=ncloc)](https://sonarcloud.io/dashboard?id=styzf_java-to-diagrams)

