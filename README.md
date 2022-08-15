# jIntl-provider-nacos
![Maven](https://img.shields.io/maven-central/v/com.laylib/jIntl-provider-nacos.svg)
![License](https://img.shields.io/github/license/LayGit/jIntl-provider-nacos.svg)

### Overview

Nacos provider for jIntl

### Installation(Maven)

Add the dependencies to your pom.xml file:

```
<dependency>
    <groupId>com.laylib</groupId>
    <artifactId>jIntl-provider-nacos</artifactId>
    <version>2.0.0</version>
</dependency>

<dependency>
    <groupId>com.laylib</groupId>
    <artifactId>jIntl</artifactId>
    <version>2.0.0</version>
</dependency>
```

Then run from the root dir of the project:

```
mvn install
```

### Installation(Gradle)

Add the dependency to your build.gradle file:

```
implementation 'com.laylib:jIntl-nacos-provider:2.0.0'
implementation 'com.laylib:jIntl:2.0.0'
```

Then load gradle changes

### Usage

#### Step 1: Create Index
Create a new config to nacos
- Data ID: index.yaml
- Group: INTL
- Format: YAML
- Content:
```yaml
global:
    - en
```

#### Step 2: Create Source
Create a new config to nacos
- Data ID: global_en.yaml
- Group: INTL
- Format: YAML
- Content:
```yaml
http:
  internalServerError: "Internal Server Error"
```

#### Step 3: Coding
```java
import com.alibaba.nacos.api.PropertyKeyConst;
import com.laylib.jintl.IntlSource;
import com.laylib.jintl.config.NacosProviderConfig;

import java.util.Locale;
import java.util.Properties;

class Application {
    public static void main(String[] args) {
        NacosProviderConfig providerConfig = new NacosProviderConfig();
        providerConfig.setGroup("INTL");
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, "127.0.0.1:8848");
        config.setConfig(properties);

        IntlSource intlSource = new IntlSource(config);
        String msg = intlSource.getMessage("http.internalServerError", Locale.ENGLISH);
        System.out.println(msg);
    }
}
```

### Using with SpringBoot
See [jIntl-spring-boot-starter](https://github.com/LayGit/jIntl-spring-boot-starter)