# dcca
diancichang assignment

## 运行方法
1. 确认 `cst仿真结果txt文件` 目录下包含 `方向1` 至 `方向8` 的数据子目录（`DataReader` 会自动适配嵌套的同名目录）。
2. 在项目根目录执行编译：
   ```bash
   javac *.java
   ```
3. 运行包含示例流程的入口类：
   ```bash
   java InterpolationTest
   ```
   程序会读取数据、初始化插值引擎，并输出多组计算结果供验证。
