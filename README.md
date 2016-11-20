## XanderPanel

[demo 下载地址][1]

平时工作需要，加上自己的业余时间做了这么一个控件。

![priview](https://github.com/XanderWang/XanderPanel/raw/master/screenshot/xander_panel.gif)


- 普通模式

就像 AlertDialog , 只不过宽度是全屏，并且对进入和退出动画做了优化，使用方法参考下面代码

``` Java
XanderPanel.Builder mBuilder = new XanderPanel.Builder(mContext);
mBuilder.setTitle("Title")
    .setIcon(R.mipmap.ic_launcher)
    .setMessage("I am Message!!!")
    .setGravity(Gravity.TOP)
    .setController("Cancel", "Ok", new PanelInterface.PanelControllerListener() {
        @Override
        public void onPanelNagetiiveClick(XanderPanel panel) {
            toast("onPanelNagetiiveClick");
        }

        @Override
        public void onPanelPositiveClick(XanderPanel panel) {
            toast("onPanelPositiveClick");
        }
    })
    .setCanceledOnTouchOutside(true);
XanderPanel xanderPanel = mBuilder.create();
xanderPanel.show();
```


- Sheet 模式

仿照 iOS 上的 ActionSheet 做的，

``` Java
XanderPanel.Builder mBuilder = new XanderPanel.Builder(mContext);
mBuilder.setSheet(
    new String[]{"I", "am", "sheet", "item"},
    true,
    new PanelInterface.SheetListener() {
        @Override
        public void onSheetItemClick(int position) {
            toast("click sheet item " + position);
        }

        @Override
        public void onSheetCancelClick() {
            toast("sheet cancel");
        }
    }
);
XanderPanel xanderPanel = mBuilder.create();
xanderPanel.show();
```

- Menu 模式

添加对 menu.xml 文件的支持， menuitem 的宽度时全屏的，现在很多 app 都是这样子的设计了。
同时还可以设置排列的样式 list 和 grid

``` Java
XanderPanel.Builder mBuilder = new XanderPanel.Builder(mContext);
mBuilder.list()
.setMenu(R.menu.main_menu, new PanelInterface.PanelMenuListener() {
    @Override
    public void onMenuClick(MenuItem menuItem) {
        toast("click MenuItem " + menuItem.getTitle());
    }
})
.setGravity(Gravity.BOTTOM)
.setCanceledOnTouchOutside(true);
XanderPanel xanderPanel = mBuilder.create();
xanderPanel.show();
```

- 自定义布局

同样支持自定义布局，自定义布局代码可参考如下代码

``` Java
XanderPanel.Builder mBuilder = new XanderPanel.Builder(mContext);
mBuilder.setCanceledOnTouchOutside(true);
mBuilder.setGravity(Gravity.BOTTOM);
View mCustomViewBottom = mInflater.inflate(R.layout.custom_layout, null);
mBuilder.setView(mCustomViewBottom);
XanderPanel xanderPanel = mBuilder.create();
xanderPanel.show();
```

- 分享模式 

项目里面有时候需要分享,顺便就加进来了,同样支持 list 和 grid 样式排列

``` Java
XanderPanel.Builder mBuilder = new XanderPanel.Builder(mContext);
mBuilder.grid(2,3)
.shareText("test share")
.setGravity(Gravity.BOTTOM)
.setCanceledOnTouchOutside(true);
XanderPanel xanderPanel = mBuilder.create();
xanderPanel.show();
```

如何使用
===
发布到了 Jcenter ， 如果你是用 Android studio 开发的话，会比较方便，在模块 build.gradle 下添加

gradle
```gradle
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.xander.panel:xanderpanel:1.3.1'
}
```

maven
``` maven
<dependency>
  <groupId>com.xander.panel</groupId>
  <artifactId>xanderpanel</artifactId>
  <version>1.3</version>
  <type>pom</type>
</dependency>
```


License
===

The MIT License (MIT)

Copyright (c) 2013

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.



[1]: http://od10jiigp.bkt.clouddn.com/demo-release_20160918.apk