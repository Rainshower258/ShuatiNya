# 📚 刷题Nya - 单词/刷题记忆助手
<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" width="120" alt="刷题Nya Logo"/>
</p>

<p align="center">
  <strong>おしえて!BadEnd先生!</strong>
</p>
  
<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License"></a>
  <a href="https://android.com"><img src="https://img.shields.io/badge/Android-8.0%2B-green.svg" alt="Android"></a>
  <img src="https://img.shields.io/badge/version-1.0.1-brightgreen.svg" alt="Version">
</p>

---

## ✨ 应用简介

**刷题Nya！**   
~~“完了完了，要考英语了，单词还没背呢！”~~   
在考试前一晚抱着手机/不想看书，于是写了一个背英语的小玩意。  
~~当然，现在它还可以刷题~~（多选，单选，判断）

### 🎯 核心亮点
- 🧠 **科学算法** - 基于艾宾浩斯遗忘曲线的5阶段复习系统
- 📝 **双模式学习** - 单词背诵 + 刷题练习
- 🎨 **自定义背景颜色** -    ('_>')
- 💾 **完全本地** - 无需联网，数据隐私安全 ~~也限制了题目选项的多元化~~


---

## 🏗️ 技术栈

### 核心技术
- **语言**: Kotlin 2.0.21
- **UI 框架**: Jetpack Compose + Material Design 3
- **数据库**: Room 2.6.1 (SQLite)
- **架构模式**: MVVM + Repository Pattern
- **异步处理**: Kotlin Coroutines + Flow
- **图片加载**: Coil

### 开发工具
- Android Studio Ladybug | 2024.2.1
- Gradle 8.9
- KSP (Kotlin Symbol Processing)
- Min SDK: 26 (Android 8.0)
- Target SDK: 35 (Android 15)
- GitHub copilot(Claude sonnet4.5) ~~大跌~~
---

## 📦 项目架构

采用 **MVVM + Clean Architecture** 设计模式：

```
app/src/main/java/com/example/english/
├── data/                   # 数据层
│   ├── database/           # Room 数据库
│   │   ├── dao/            # 数据访问对象
│   │   ├── entity/         # 数据库实体
│   │   └── AppDatabase.kt  # 数据库实例
│   ├── repository/         # 数据仓库
│   ├── model/              # 数据模型
│   ├── parser/             # 文本解析器
│   └── algorithm/          # SRS 算法实现
├── ui/                     # UI 层
│   ├── screens/            # 各功能界面
│   ├── components/         # 可复用组件
│   ├── theme/              # 主题配置
│   └── viewmodel/          # ViewModel
├── util/                   # 工具类
└── service/                # 业务服务

核心模块：
- WordRepository - 单词数据管理
- QuestionRepository - 题目数据管理  
- StudyService - 学习逻辑
- ReviewManager - 复习调度

测试机型：
-REDMI K80 Pro Xiaomi HyperOS 2.0.102.0
-Redmi K50 Ultra MIUI 14.0.7
-联想小新Pad Pro 2021 ZUI 15.0.423
-其他机型/系统还没测试过，上述机型（手机/平板）均可正常运行
```
~~其他机型应该也可，大概？？？~~
---

## 🎯 核心功能

### 📖 智能词库管理
- **自定义词库**：创建专属词库，按主题、难度或来源分类整理
- **批量导入**：支持文本批量导入，轻松把笔记、文章中的生词一键收录
- **单词 + 短语**：不仅能学单词，还能掌握地道的英语短语搭配
- **刷题**：创建库时，有背单词/刷题选项。刷题模式支持单/多选，判断题

### 🧠 科学记忆系统
- **复习模式**：如题 ~~我在使用时都是直接重复刷来复习的，没经过测试orz~~
- **间隔重复算法**：智能调整复习间隔，让记忆更持久
- **复习阶段追踪**：清晰了解每个单词的掌握程度

### ⏰ 智能提醒
- **每日复习提醒**：定时推送，养成学习好习惯
- **自定义时间**：选择最适合你的复习时间
~~除应用通知模式外，均有严重bug,暂不推荐使用~~

- ~~26.2.3：暂时禁用~~

### 💾 数据备份与迁移
- **本地备份**：一键备份所有学习数据到本地存储
- **快速恢复**：支持从备份快速恢复数据
- **备份管理**：查看所有历史备份，自由选择恢复或删除

#### 📦 备份文件说明
每个备份包含以下 SQLite 数据库文件：
- **`*.db`** - 主数据库文件（包含所有词库、单词、学习记录）
- **`*.db-shm`** - 共享内存索引文件（临时文件，提升性能）
- **`*.db-wal`** - 预写日志文件（确保数据完整性和事务安全）

**重要提示：**
- ✅ 恢复备份时，只需选择 `.db` 文件，关联文件会自动处理
- ✅ 删除备份会同时清理所有关联文件（.db、.db-shm、.db-wal）
- ⚠️ 卸载应用会删除所有备份，重要数据请提前导出
- ⚠️ 手动移动备份文件可能导致权限问题，遇到时请使用「修复权限」功能

---

## 🚀 快速开始

###下载base.apk并安装✌️

### 环境要求
- Android Studio Ladybug (2024.2.1) 或更高版本
- JDK 11 或更高版本
- Android SDK 26+（Android 8.0+）
- Gradle 8.9+

### 克隆并运行
```bash
# 1. 克隆项目
git clone https://github.com/Rainshower258/ShuatiNya.git
cd ShuatiNya

# 2. 使用 Gradle 构建
./gradlew assembleDebug

# 3. 或在 Android Studio 中直接运行
# File → Open → 选择项目目录
# 等待 Gradle 同步完成
# 点击 Run (Shift+F10)
```

### 项目配置
- ✅ 无需额外配置
- ✅ 不需要 API Key ~~后续会增加导入api（需自备）的ai自动总结题库功能~~
- ✅ 不需要网络连接
- ✅ 所有数据本地存储

---

## 📖 使用指南

### 1️⃣ 创建词库
打开应用 → 点击右下角 **"+"** → 选择模式 → 输入库名称 → 创建

### 2️⃣ 添加单词
主界面 → 点击对应词/题库 **"导入"** → **"粘贴单词文本"** → 可再次添加单词/题目（自动排除重复项）

**支持的导入格式：**
```
<W>
英文：abandon
中文对照：放弃；遗弃
词性：v.
音标：/əˈbændən/
类型：word
</W>
```
在导入界面，点击 **"AI提示词"** 右侧的按钮，自动复制prompt。在各大ai应用中上传待提取的图片/文件，粘贴并发送该提示词

### 3️⃣ 开始学习
选择词库 → 点击 **"开始学习"** → 根据提示作答 → 系统自动记录进度

### 4️⃣ 按时复习
收到复习提醒 → 打开应用 → 复习到期的单词 → 巩固记忆
主界面 → 复习中心 → 进入 → 点击想要复习的词/题库 → 开始复习
~~PS:提醒通知功能有bug,暂时不建议使用，包括日历、闹钟~~

### 5️⃣ 数据备份
设置 → 数据管理 → 点击 **"立即备份"** 进行词/题库，学习进度的备份

（若备份文件未显示，尝试重新进入设置/打开应用）

注意：备份的文件请上传/分享/复制到其他地方保存好，在卸载/程序故障时备份会跟随删除。需要恢复时 再次将备份文件导入回备份路径，并点击 备份历史 中对应备份的 ！ → 修复权限 后再恢复
---

## 📱 系统要求

- **Android**: 8.0 (API 26) 及以上
- **存储空间**: 约 20MB  

---

## 🔒 隐私与安全

- ✅ **完全本地化**：所有数据存储在设备本地 SQLite 数据库
- ✅ **无网络请求**：应用不进行任何网络通信
- ✅ **数据隔离**：使用 Room 数据库加密存储
- ✅ **无广告无追踪**：🤣
- ✅ **自由练习**：点击 查看词/题库 进行背题

---

## 📜 更新日志

### v1.0.1 (2026-02-03)
- 🔧 **安全修复**：移除 `fallbackToDestructiveMigration()`，防止数据库升级时意外丢失数据
- ⚡ **性能优化**：全面优化 Composable 重组性能，使用 `remember` 缓存 Repository/Service 实例
- 🪵 **日志统一**：统一使用 `AppLogger` 替代分散的 `android.util.Log`，Release 版本更安全
- 🐛 **错误处理**：完善所有 ViewModel 的异常捕获和日志记录 ~~不再静默吞错误了~~
- 🧹 **代码清理**：移除未使用的 import，清理冗余代码

### v1.0.0 (2026-02)
- 🎉 首次发布 ~~想发，但看不下去了🤣~~
- ✨ 完整的单词管理系统
- ✨ 刷题练习模式
- ✨ 本地数据备份功能
- ✨ 深色/浅色主题支持

---

## 🫓 正在画的大饼
- [ ] 修复学习提醒功能 ~~日历/闹钟模式还是有bug~~
- [ ] 自定义备份路径
- [ ] 查看备份文件（跳转文件管理/MT管理器）
- [ ] 优化复习中心相关功能
- [ ] 使用api提取题目（不用再多个应用来回跳了）
- [x] ~~优化并重构史山😢~~ 初版重构完成！

---

## 🤔 我学到了什么？

### 🤖 Vibe Coding 从入门到入土
学习路线：从 AI Chat 复制粘贴 → Copilot（GitHub 教育认证） → Continue（DS API） → Claude Code CLI（Skills 的使用，多终端/角色协作） → Antigravity/Clawdbot（尝试移动端发布任务→全自动办公）~~token 烧的太快了，放弃了😭~~

### 🏗️ 项目架构从零到一
**MVVM + Clean Architecture**，写完这个项目，理解了各层的职责：
- **Entity** (数据库实体)：定义数据长啥样
- **DAO** (数据访问)：怎么从数据库拿数据
- **Repository** (仓库层)：对上层隐藏数据来源（可以换数据库不影响业务）
- **Service** (业务层)：核心逻辑 ~~ViewModel 不该写复杂业务😢~~
- **ViewModel**：只负责管理 UI 状态，调用 Service 就完事
- **Screen** (UI层)：纯展示，调用 ViewModel

~~之前写的代码全在 Activity 里，现在想想确实是史山~~

### 📐 从需求到代码的完整流程
- ~~想成为PM领域高手~~

第一次完整走完一个项目的全生命周期（~~终端学生管理系统领域大神来咯~~）：
1. **需求分析**：~~考前一晚想背单词~~ → 明确核心功能（背  单  词）
2. **技术选型**：Jetpack Compose + Room + Coroutines（跟着 Google 官方文档走）
3. **数据建模**：先画 ER 图，确定 Entity 结构（单词表/题库表/学习记录表的关系）
4. **接口设计**：定义 DAO 的增删改查方法，Repository 的业务接口
5. **逻辑实现**：Service 层实现复习算法，测试计算复习时间是否正确（真的正确吗？🤫）
6. **UI 开发**：用 Compose 写界面，ViewModel 绑定数据
7. **测试迭代**：在真机上跑，发现 bug → 改 → 再测
8. **代码重构**：让 AI 审查代码，修掉隐藏的坑（比如数据库迁移的大坑）

**最大收获**：**先设计后编码**，而不是边写边想。虽然中间还是改了不少设计，但至少有个大框架在 ~~（比无脑写强多了）~~ 

### 🧩 按模块化完成项目的好处

**终于理解为什么要模块化了**：

之前写代码习惯"想到哪写到哪"，结果就是：
- 想加个功能，发现要改 10 个文件
- 改了一个 bug，另外 3 个地方炸了
- 复习算法和 UI 逻辑混在一起，看着头疼

and vibe项目一股脑写完prompt就开跑：~~曾经想直接跑一个fgo脚本出来的😔~~
- 功能需求葫芦娃救爷爷一样地往里塞
- 重构、加功能难，bug越写越多

**这次按模块拆分后**：
1. **数据层 (data)** 先搞定 → 确保数据库能正常存取
2. **业务层 (service)** 再写 → 艾宾浩斯算法单独实现，可以独立测试
3. **UI 层 (ui)** 最后做 → 只管展示数据，逻辑不用操心

**实际体验**：
- ✅ **改 bug 范围可控**：算法有问题就改 `StudyService`，UI 出错就看 Screen
- ✅ **可以并行开发**：我可以先写完 Repository，然后让 AI 帮我写 ViewModel ~~（真香）~~
- ✅ **代码复用率高**：`ReviewManager` 写一次，单词和题库都能用
- ✅ **测试更简单**：不用启动整个 APP，直接测某个 Service 的逻辑

**踩过的坑**：
- ❌ 一开始没规划好模块边界，`ViewModel` 里写了一堆业务逻辑 ~~（后来重构吐了）~~
- ❌ 跨模块依赖没理清楚，导致循环引用编译报错
- ❌ 想偷懒直接在 UI 层操作数据库，结果 Compose 重组时数据乱飞

**现在的开发流程**：
```
1. 需求来了 → 先想它属于哪个模块（数据/业务/UI）
2. 从下往上写：Entity → DAO → Repository → Service → ViewModel → Screen
3. 每个模块写完立刻测，不等到最后一起炸
4. 重构时只动对应模块，其他地方不用慌
```

**最大感悟**：模块化完成项目 **易如反掌呐**

### 🏗️ 让 AI 当架构师
这次重构最大的收获：**与其自己瞎改，不如先让 AI 审查一遍代码**。不熟悉安卓开发的情况下，直接丢个 prompt 让它当安全审查员，指出来的问题比我自己 review 三遍发现的还多 ~~（其实自己也看不出来什么吧）~~

主要发现的坑：
- `fallbackToDestructiveMigration()` 这玩意会在数据库升级失败时**直接删库跑路**，导入的词库和数据说没就没 ~~（还好我还没发布就发现了）~~
- Composable 函数里直接 new Service，每次重组都 new 一遍，虽然影响不大但看着难受

### 📝 Compose 性能优化
学会了 `remember` 的正确用法：
```kotlin
// ❌ 之前的写法：每次重组都创建新实例
val service = SomeService(database.dao())

// ✅ 正确的写法：缓存起来，避免重复创建
val service = remember {
    SomeService(database.dao())
}
```

---

### 🪵 统一日志系统
把散落各处的 `android.util.Log` 统一换成了自定义的 `AppLogger`：
- Debug 模式：正常输出日志，方便调试
- Release 模式：自动禁用，不会泄露敏感信息 ~~（虽然也没啥敏感的）~~

### 📋 数据库迁移脚本
搞懂了 Room 的 Migration 机制：
- 只有改 Entity（表结构）才需要写迁移脚本
- 改 UI、改逻辑都不需要
- 迁移脚本其实就一行 SQL😢

### 🎨 从设计到实现的思考

**学到的坑**：
- `LazyColumn` 的 key 参数很重要，不然列表滚动会出鬼畜 

  ~~（最开始想要设计滚动条来着，但vibe产物永远显示不出来，手动到处找滚动条方案也搜不到）~~
- `remember` 要配合 `key`，否则重组后数据会乱
- `ViewModel` 不能直接持有 Context，会内存泄漏

### 🔧 Markdown 大学习
**果然还是写着写着就能学会了**


### 📚 后续学习大饼
- 真正深入理解项目，“高”手敲占比来复现项目 ~~能做到吗😯我的天哪😫~~
- 正好在开学就要学sql了，看看有没有能优化的地方吧



---

## 🙏 致谢

### 开源技术
本项目使用了以下优秀的开源技术：

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代化 Android UI 工具包
- [Room Database](https://developer.android.com/training/data-storage/room) - SQLite 抽象层
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - 异步编程框架
- [Coil](https://coil-kt.github.io/coil/) - 图片加载库
- [Material Design 3](https://m3.material.io/) - Google 设计系统
- ~~天哪copilot大人😭~~

---

## 📄 开源许可

本项目采用 [MIT 许可证](LICENSE) 开源。

```
MIT License

Copyright (c) 2026 sun6 (Rainshower258)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 📬 联系方式

- **GitHub**: [@Rainshower258](https://github.com/Rainshower258)
- **Email**: s1073454586@163.com
- **QQ群**: 732655373 ~~求拷打😭~~

---

<p align="center">
  <strong>Made by Rainshower258</strong>
</p>

<p align="center">
  <sub>© 2026 刷题Nya. All rights reserved.</sub>
</p>

