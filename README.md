# TextInputView


A different approach to the [TextInputLayout](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html).
The TextInputView takes less space for repositioning its hint and allows for more customization. 
Nonetheless it adapts to the Material Design Guidelines by adapting primary- and accent colors as well as the style of the target hint.

<img src="https://github.com/Faltenreich/TextInputView/blob/develop/preview.gif" width="200">

## Features
- Keep the hint of an EditText visible to the user at all times
- Prevent unused space on an empty EditText
- Customize color, style and animation
- No dependencies other than org.jetbrains.kotlin:kotlin-stdlib-jre7, since this library is completely written in Kotlin

## Getting Started

##### Gradle
```gradle
dependencies {
    implementation 'com.github.Faltenreich:TextInputView:1.0.0'
}
```

##### XML
```xml
<com.faltenreich.textinputview.TextInputView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:overlapAnimation="animate">
    
    <EditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="I will move on typing"/>
        
</com.faltenreich.textinputview.TextInputView>
```

##### Java
```java
public class MainActivity extends AppCompatActivity {
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TextInputView textInputView = new TextInputView(getContext(), findViewById(R.id.editText));
    }
}

```

##### Kotlin
```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val textInputView = TextInputView(context, findViewById(R.id.editText))
    }
}
```

## Advanced usage

### Configuration

Property | Type | Description | Default
--- | --- | --- | ---
android:textColor | color | Applied when embedded EditText loses focus | textColor of the hint of the embedded EditText
android:textSize | dimension | Text size of the hint | textSize of the hint of the embedded EditText
android:tint | color | Applied when embedded EditText gets focused | tint of the hint of the embedded EditText
animationDurationMillis | integer | The time of the move and overlap animation in ms | 300
hintPadding | dimension | The space between input and hint to define the overlap | TODO
moveAnimation | enum | Applied when the input has been cleared or re-filled (options: toggle, animate) | animate
overlapAnimation | enum | Applied when the input text overlaps the hint including its padding (options: toggle, animation, push) | toggle

## License

Copyright 2018 Philipp Fahlteich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.