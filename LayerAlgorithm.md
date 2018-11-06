## 对分层算法改进 https://zhuanlan.zhihu.com/p/48466788
论文里面的分层算法主要是用了隐函数来求解。但是对本例中没有隐函数的情况只能用点阵和相应的坐标了。<br>
缩进对应的点的位置，主要是这样计算的：当前存在点
```math
P_0 = (x_0, y_0)
```
已知存在若干层n，则对应的第n层的位置坐标定为：
```math
P_t = (x_t, y_t)
```
用P_0表示P_t的方法可以计算得到。涉及的是直线之间的关系。打印序列前后的点坐标分别为：
```math
P_{previous} = (x_p, y_p)\qquad P_{latter} = (x_l, y_l)
```
构造直线：
```math
\bm{{\omega_1}^T x}+b_1=0\qquad
\bm{{\omega_1}}=
\left(
\begin{array}{ccc}
    y_l-y_0\\
    x_0-x_l
\end{array}
\right)
\qquad
\bm{{x}}=
\left(
\begin{array}{ccc}
    x\\
    y
\end{array}
\right)
\qquad
b_1={x_l}{y_0}-{x_0}{y_l}
```
找一个基准点求解距离：
```math
(\overline{x},\overline{y})\qquad
\overline{x}=\sum_{i=1}^m{x_i},
\overline{y}=\sum_{i=1}^m{y_i}
```
则点到直线的距离公式可以表述为：
```math
L = \frac{\vert\bm{{\omega_1}^T{x_0}}+b_1\vert}{\vert\vert\bm{\omega_1\vert\vert}}
\qquad
\bm{{x_0}}=
\left(
\begin{array}{ccc}
    \overline{x}\\
    \overline{y}
\end{array}
\right)
```
列出等式：
```math
\frac{\vert\bm{{\omega_1}^T{x_0}}+b_1\vert}{\vert\vert\bm{\omega_1\vert\vert}} = \frac{\vert\bm{{\omega_1}^T{x_0}}+C_1\vert}{\vert\vert\bm{\omega_1\vert\vert}}+(n-1)d
```
分情况计算得到结果为：
```math
C_1=b_1\pm(n-1)d\vert\vert\bm{{\omega_1}}\vert\vert
```
等式左边的
```math
\bm{{\omega_1}^T{x_0}}+b_1
```
结果为正时用减，负时用加。
同理可以得到：
```math
\bm{{\omega_2}^T x}+b_2=0\qquad
\bm{{\omega_2}}=
\left(
\begin{array}{ccc}
    y_0-y_p\\
    x_p-x_0
\end{array}
\right)
\qquad
\bm{{x}}=
\left(
\begin{array}{ccc}
    x\\
    y
\end{array}
\right)
\qquad
b_2={x_0}{y_p}-{x_p}{y_0}
```
同理：
```math
C_2=b_2\pm(n-1)d\vert\vert\bm{\omega_2}\vert\vert
```
联立方程得到：
```math
\left\{
\begin{aligned}
\bm{{\omega_2}^T{x}}+C_2=0\\
\bm{{\omega_1}^T{x}}+C_1=0
\end{aligned}
\right.
```
矩阵形式求解为：
```math
\left(
\begin{array}{ccc}
    {\bm{{\omega_1}^T}}\\
    {\bm{{\omega_2}^T}}
\end{array}
\right)\bm{x}=
\left(
\begin{array}{ccc}
    {-C_1}\\
    {-C_2}
\end{array}
\right)
```
消元求解即可。但是会遇到没有解的问题（两条直线斜率相等）<br>
这个时候使用斜率乘法，向量相乘的方法来求解即可。具体解法也是通过构造解空间和矩阵来得到的。<br>
把原式左边给展开得到：
```math
\left(
\begin{array}{ccc}
    \omega_{11}&\omega_{12}\\
    \omega_{21}&\omega_{22}
\end{array}
\right)
\left(
\begin{array}{ccc}
    x_{t}\\
    y_{t}
\end{array}
\right)=
\left(
\begin{array}{ccc}
    {-C_1}\\
    {-C_2}
\end{array}
\right)
```
根据斜率之间的关系，得到关系式：
```math
\frac{\omega_{11}}{\omega_{21}}=
\frac{\omega_{12}}{\omega_{22}}=
\frac{C_1}{C_2}
```
对C_1和C_2必须要满足这个等式不然就平行不相交了。这样问题简化为P_0在直线上的投影点问题。由于斜率的关系，首先找到平行于直线的向量：
```math
\overrightarrow{n}=(\omega_{12}, -\omega_{11})\qquad
\overrightarrow{m}=(x_t-x_0, y_t-y_0)
```
向量垂直，则：
```math
\overrightarrow{n}\cdot\overrightarrow{m}=0
```
方程为：
```math
\bm{{\omega_3}^Tx}+C_3=0\qquad
\left\{
\begin{aligned}
\bm{{\omega_3}}=
\left(
\begin{array}{ccc}
    {\omega_{12}}\\
    {-\omega_{11}}
\end{array}
\right)\\
C_3=\omega_{11}y_0-\omega_{12}x_0
\end{aligned}
\right.
```
和\omega_1联立即可求解：
```math
\left(
\begin{array}{ccc}
    {\bm{{\omega_1}^T}}\\
    {\bm{{\omega_3}^T}}
\end{array}
\right)\bm{x}=
\left(
\begin{array}{ccc}
    {-C_1}\\
    {-C_3}
\end{array}
\right)
```