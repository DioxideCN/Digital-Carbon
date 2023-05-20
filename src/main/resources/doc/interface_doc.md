
**接口文档**


**简介**：<p>数碳智能解耦文档</p>


**HOST**:localhost:8080

**联系人**:数碳智能

**Version**:1.0.0

**接口路径**：/v2/api-docs


# UserController

## 邮箱登录


**接口描述**:


**接口地址**:`/api/user/auth/login/email`


**请求方式**：`POST`


**consumes**:`["application/json"]`


**produces**:`["*/*"]`



**请求参数**：

| 参数名称         | 参数说明     |     in |  是否必须      |  数据类型  |  schema  |
| ------------ | -------------------------------- |-----------|--------|----|--- |
|email| email  | query | true |string  |    |
|password| password  | query | true |string  |    |

**响应示例**:

```json
{
	"code": 0,
	"data": {},
	"msg": ""
}
```

**响应参数**:


| 参数名称         | 参数说明                             |    类型 |  schema |
| ------------ | -------------------|-------|----------- |
|code| 响应码  |integer(int32)  | integer(int32)   |
|data| 返回数据体  |object  |    |
|msg| 附加消息  |string  |    |





**响应状态**:


| 状态码         | 说明                            |    schema                         |
| ------------ | -------------------------------- |---------------------- |
| 200 | OK  |响应结果«object»|
| 201 | Created  ||
| 401 | Unauthorized  ||
| 403 | Forbidden  ||
| 404 | Not Found  ||
## 手机验证码登录


**接口描述**:


**接口地址**:`/api/user/auth/login/phone`


**请求方式**：`POST`


**consumes**:`["application/json"]`


**produces**:`["*/*"]`



**请求参数**：

| 参数名称         | 参数说明     |     in |  是否必须      |  数据类型  |  schema  |
| ------------ | -------------------------------- |-----------|--------|----|--- |
|code| code  | query | true |string  |    |
|phone| phone  | query | true |string  |    |

**响应示例**:

```json
{
	"code": 0,
	"data": {},
	"msg": ""
}
```

**响应参数**:


| 参数名称         | 参数说明                             |    类型 |  schema |
| ------------ | -------------------|-------|----------- |
|code| 响应码  |integer(int32)  | integer(int32)   |
|data| 返回数据体  |object  |    |
|msg| 附加消息  |string  |    |





**响应状态**:


| 状态码         | 说明                            |    schema                         |
| ------------ | -------------------------------- |---------------------- |
| 200 | OK  |响应结果«object»|
| 201 | Created  ||
| 401 | Unauthorized  ||
| 403 | Forbidden  ||
| 404 | Not Found  ||
## 用户名登录


**接口描述**:


**接口地址**:`/api/user/auth/login/username`


**请求方式**：`POST`


**consumes**:`["application/json"]`


**produces**:`["*/*"]`



**请求参数**：

| 参数名称         | 参数说明     |     in |  是否必须      |  数据类型  |  schema  |
| ------------ | -------------------------------- |-----------|--------|----|--- |
|password| password  | query | true |string  |    |
|username| username  | query | true |string  |    |

**响应示例**:

```json
{
	"code": 0,
	"data": {},
	"msg": ""
}
```

**响应参数**:


| 参数名称         | 参数说明                             |    类型 |  schema |
| ------------ | -------------------|-------|----------- |
|code| 响应码  |integer(int32)  | integer(int32)   |
|data| 返回数据体  |object  |    |
|msg| 附加消息  |string  |    |





**响应状态**:


| 状态码         | 说明                            |    schema                         |
| ------------ | -------------------------------- |---------------------- |
| 200 | OK  |响应结果«object»|
| 201 | Created  ||
| 401 | Unauthorized  ||
| 403 | Forbidden  ||
| 404 | Not Found  ||
## 用户注册


**接口描述**:


**接口地址**:`/api/user/auth/register`


**请求方式**：`POST`


**consumes**:`["application/json"]`


**produces**:`["*/*"]`


**请求示例**：
```json
{
	"code": "",
	"email": "",
	"password": "",
	"phone": "",
	"username": ""
}
```


**请求参数**：

| 参数名称         | 参数说明     |     in |  是否必须      |  数据类型  |  schema  |
| ------------ | -------------------------------- |-----------|--------|----|--- |
|userRegisterParam| 用户注册时需要传递该对象中的所有参数  | body | true |UserRegister对象  | UserRegister对象   |

**schema属性说明**



**UserRegister对象**

| 参数名称         | 参数说明     |     in |  是否必须      |  数据类型  |  schema  |
| ------------ | -------------------------------- |-----------|--------|----|--- |
|code| 手机验证码  | body | true |string  |    |
|email| 用户邮箱  | body | false |string  |    |
|password| 密码  | body | true |string  |    |
|phone| 联系方式  | body | true |string  |    |
|username| 用户名  | body | true |string  |    |

**响应示例**:

```json
{
	"code": 0,
	"data": {},
	"msg": ""
}
```

**响应参数**:


| 参数名称         | 参数说明                             |    类型 |  schema |
| ------------ | -------------------|-------|----------- |
|code| 响应码  |integer(int32)  | integer(int32)   |
|data| 返回数据体  |object  |    |
|msg| 附加消息  |string  |    |





**响应状态**:


| 状态码         | 说明                            |    schema                         |
| ------------ | -------------------------------- |---------------------- |
| 200 | OK  |响应结果«object»|
| 201 | Created  ||
| 401 | Unauthorized  ||
| 403 | Forbidden  ||
| 404 | Not Found  ||
## 手机号请求验证码


**接口描述**:


**接口地址**:`/api/user/auth/send`


**请求方式**：`POST`


**consumes**:`["application/json"]`


**produces**:`["*/*"]`



**请求参数**：

| 参数名称         | 参数说明     |     in |  是否必须      |  数据类型  |  schema  |
| ------------ | -------------------------------- |-----------|--------|----|--- |
|phone| phone  | query | true |string  |    |

**响应示例**:

```json
{
	"code": 0,
	"data": {},
	"msg": ""
}
```

**响应参数**:


| 参数名称         | 参数说明                             |    类型 |  schema |
| ------------ | -------------------|-------|----------- |
|code| 响应码  |integer(int32)  | integer(int32)   |
|data| 返回数据体  |object  |    |
|msg| 附加消息  |string  |    |





**响应状态**:


| 状态码         | 说明                            |    schema                         |
| ------------ | -------------------------------- |---------------------- |
| 200 | OK  |响应结果«object»|
| 201 | Created  ||
| 401 | Unauthorized  ||
| 403 | Forbidden  ||
| 404 | Not Found  ||
## 获取用户权限组


**接口描述**:


**接口地址**:`/api/user/profile/permission-group`


**请求方式**：`POST`


**consumes**:`["application/json"]`


**produces**:`["*/*"]`



**请求参数**：

| 参数名称         | 参数说明     |     in |  是否必须      |  数据类型  |  schema  |
| ------------ | -------------------------------- |-----------|--------|----|--- |
|username| username  | query | true |string  |    |

**响应示例**:

```json
{
	"code": 0,
	"data": {},
	"msg": ""
}
```

**响应参数**:


| 参数名称         | 参数说明                             |    类型 |  schema |
| ------------ | -------------------|-------|----------- |
|code| 响应码  |integer(int32)  | integer(int32)   |
|data| 返回数据体  |object  |    |
|msg| 附加消息  |string  |    |





**响应状态**:


| 状态码         | 说明                            |    schema                         |
| ------------ | -------------------------------- |---------------------- |
| 200 | OK  |响应结果«object»|
| 201 | Created  ||
| 401 | Unauthorized  ||
| 403 | Forbidden  ||
| 404 | Not Found  ||
## 获取用户权限组所属企业


**接口描述**:


**接口地址**:`/api/user/profile/pg-of-company`


**请求方式**：`POST`


**consumes**:`["application/json"]`


**produces**:`["*/*"]`



**请求参数**：

| 参数名称         | 参数说明     |     in |  是否必须      |  数据类型  |  schema  |
| ------------ | -------------------------------- |-----------|--------|----|--- |
|username| username  | query | true |string  |    |

**响应示例**:

```json
{
	"code": 0,
	"data": {},
	"msg": ""
}
```

**响应参数**:


| 参数名称         | 参数说明                             |    类型 |  schema |
| ------------ | -------------------|-------|----------- |
|code| 响应码  |integer(int32)  | integer(int32)   |
|data| 返回数据体  |object  |    |
|msg| 附加消息  |string  |    |





**响应状态**:


| 状态码         | 说明                            |    schema                         |
| ------------ | -------------------------------- |---------------------- |
| 200 | OK  |响应结果«object»|
| 201 | Created  ||
| 401 | Unauthorized  ||
| 403 | Forbidden  ||
| 404 | Not Found  ||
## 用户修改联系方式


**接口描述**:


**接口地址**:`/api/user/profile/phone`


**请求方式**：`POST`


**consumes**:`["application/json"]`


**produces**:`["*/*"]`



**请求参数**：

| 参数名称         | 参数说明     |     in |  是否必须      |  数据类型  |  schema  |
| ------------ | -------------------------------- |-----------|--------|----|--- |
|code| code  | query | true |string  |    |
|newPhone| newPhone  | query | true |string  |    |
|oldPhone| oldPhone  | query | true |string  |    |
|username| username  | query | true |string  |    |

**响应示例**:

```json
{
	"code": 0,
	"data": {},
	"msg": ""
}
```

**响应参数**:


| 参数名称         | 参数说明                             |    类型 |  schema |
| ------------ | -------------------|-------|----------- |
|code| 响应码  |integer(int32)  | integer(int32)   |
|data| 返回数据体  |object  |    |
|msg| 附加消息  |string  |    |





**响应状态**:


| 状态码         | 说明                            |    schema                         |
| ------------ | -------------------------------- |---------------------- |
| 200 | OK  |响应结果«object»|
| 201 | Created  ||
| 401 | Unauthorized  ||
| 403 | Forbidden  ||
| 404 | Not Found  ||
## 用户修改头像


**接口描述**:


**接口地址**:`/api/user/profile/portrait`


**请求方式**：`POST`


**consumes**:`["application/json"]`


**produces**:`["*/*"]`



**请求参数**：

| 参数名称         | 参数说明     |     in |  是否必须      |  数据类型  |  schema  |
| ------------ | -------------------------------- |-----------|--------|----|--- |
|number| number  | query | true |integer  |    |
|username| username  | query | true |string  |    |

**响应示例**:

```json
{
	"code": 0,
	"data": {},
	"msg": ""
}
```

**响应参数**:


| 参数名称         | 参数说明                             |    类型 |  schema |
| ------------ | -------------------|-------|----------- |
|code| 响应码  |integer(int32)  | integer(int32)   |
|data| 返回数据体  |object  |    |
|msg| 附加消息  |string  |    |





**响应状态**:


| 状态码         | 说明                            |    schema                         |
| ------------ | -------------------------------- |---------------------- |
| 200 | OK  |响应结果«object»|
| 201 | Created  ||
| 401 | Unauthorized  ||
| 403 | Forbidden  ||
| 404 | Not Found  ||
