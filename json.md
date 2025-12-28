{
  "openapi": "3.0.1",
  "info": {
    "title": "HNU校园交流平台 API文档",
    "description": "HNU校园交流平台后端API接口文档",
    "contact": {
      "name": "HNU Campus Platform",
      "email": "support@hnu.edu.cn"
    },
    "license": {
      "name": "Apache 2.0",
      "url": "https://www.apache.org/licenses/LICENSE-2.0.html"
    },
    "version": "1.0.0"
  },
  "servers": [
    {
      "url": "http://localhost:8080",
      "description": "Generated server url"
    }
  ],
  "tags": [
    {
      "name": "管理员管理",
      "description": "管理员审核、管理帖子、禁言用户等接口"
    },
    {
      "name": "用户管理",
      "description": "用户个人信息相关接口"
    },
    {
      "name": "评论管理",
      "description": "评论发布、点赞等接口"
    },
    {
      "name": "认证管理",
      "description": "用户注册、登录等认证相关接口"
    },
    {
      "name": "帖子管理",
      "description": "帖子发布、查询、搜索等接口"
    }
  ],
  "paths": {
    "/api/v1/users/me": {
      "get": {
        "tags": [
          "用户管理"
        ],
        "summary": "获取个人信息",
        "description": "获取当前登录用户的个人信息",
        "operationId": "getMyInfo",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseUserInfoDTO"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      },
      "put": {
        "tags": [
          "用户管理"
        ],
        "summary": "修改个人信息",
        "description": "修改当前登录用户的个人信息",
        "operationId": "updateMyInfo",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserUpdateDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseVoid"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/posts": {
      "get": {
        "tags": [
          "帖子管理"
        ],
        "summary": "获取帖子列表",
        "description": "获取帖子列表，支持分页和按分类筛选",
        "operationId": "getPostList",
        "parameters": [
          {
            "name": "categoryId",
            "in": "query",
            "description": "分类ID",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32"
            },
            "example": 1
          },
          {
            "name": "page",
            "in": "query",
            "description": "页码，从1开始",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 1
            },
            "example": 1
          },
          {
            "name": "size",
            "in": "query",
            "description": "每页数量",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 10
            },
            "example": 10
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseListPostListDTO"
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "帖子管理"
        ],
        "summary": "发布帖子",
        "description": "发布新帖子，标题限制4-20字，分类必须合法",
        "operationId": "createPost",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/PostCreateDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseLong"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/posts/{id}/like": {
      "post": {
        "tags": [
          "帖子管理"
        ],
        "summary": "点赞/取消点赞帖子",
        "description": "点赞或取消点赞帖子",
        "operationId": "toggleLike",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "帖子ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseBoolean"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/comments": {
      "post": {
        "tags": [
          "评论管理"
        ],
        "summary": "发布评论",
        "description": "对帖子发布评论或回复评论",
        "operationId": "createComment",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/CommentCreateDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseLong"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/comments/{id}/like": {
      "post": {
        "tags": [
          "评论管理"
        ],
        "summary": "点赞/取消点赞评论",
        "description": "点赞或取消点赞评论",
        "operationId": "toggleLike_1",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "评论ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseBoolean"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/auth/send-verify-code": {
      "post": {
        "tags": [
          "认证管理"
        ],
        "summary": "发送验证码",
        "description": "发送手机验证码到Redis，有效期5分钟",
        "operationId": "sendVerifyCode",
        "parameters": [
          {
            "name": "phone",
            "in": "query",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/auth/register": {
      "post": {
        "tags": [
          "认证管理"
        ],
        "summary": "用户注册",
        "description": "用户注册接口，包含手机验证码校验逻辑",
        "operationId": "register",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RegisterDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseLong"
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/auth/login": {
      "post": {
        "tags": [
          "认证管理"
        ],
        "summary": "用户登录",
        "description": "用户登录接口，返回JWT Token",
        "operationId": "login",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/LoginDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseLoginResponseDTO"
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/admin/users/mute": {
      "post": {
        "tags": [
          "管理员管理"
        ],
        "summary": "禁言用户",
        "description": "管理员禁言或解除禁言用户",
        "operationId": "muteUser",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserMuteDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseVoid"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/admin/auth/review": {
      "post": {
        "tags": [
          "管理员管理"
        ],
        "summary": "审核注册信息",
        "description": "审核用户注册信息，通过或拒绝",
        "operationId": "reviewAuth",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/AuthReviewDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseVoid"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/users/me/posts": {
      "get": {
        "tags": [
          "用户管理"
        ],
        "summary": "查看我的发帖",
        "description": "获取当前用户发布的帖子列表，支持分页",
        "operationId": "getMyPosts",
        "parameters": [
          {
            "name": "page",
            "in": "query",
            "description": "页码，从1开始",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 1
            },
            "example": 1
          },
          {
            "name": "size",
            "in": "query",
            "description": "每页数量",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 10
            },
            "example": 10
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseListPostListDTO"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/posts/{id}": {
      "get": {
        "tags": [
          "帖子管理"
        ],
        "summary": "获取帖子详情",
        "description": "获取帖子详情，包含评论列表",
        "operationId": "getPostDetail",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "帖子ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponsePostDetailDTO"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "帖子管理"
        ],
        "summary": "删除帖子",
        "description": "逻辑删除帖子，只有作者本人可以删除",
        "operationId": "deletePost",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "帖子ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseVoid"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/posts/search": {
      "get": {
        "tags": [
          "帖子管理"
        ],
        "summary": "搜索帖子",
        "description": "支持模糊搜索（SQL LIKE实现），可预留ES接口",
        "operationId": "searchPosts",
        "parameters": [
          {
            "name": "keyword",
            "in": "query",
            "description": "搜索关键词",
            "required": true,
            "schema": {
              "type": "string"
            },
            "example": "自行车"
          },
          {
            "name": "categoryId",
            "in": "query",
            "description": "分类ID",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32"
            },
            "example": 1
          },
          {
            "name": "page",
            "in": "query",
            "description": "页码，从1开始",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 1
            },
            "example": 1
          },
          {
            "name": "size",
            "in": "query",
            "description": "每页数量",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 10
            },
            "example": 10
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseListPostListDTO"
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/posts/hot": {
      "get": {
        "tags": [
          "帖子管理"
        ],
        "summary": "获取热搜帖子",
        "description": "返回热度前10的帖子，使用Redis缓存",
        "operationId": "getHotPosts",
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseListPostListDTO"
                }
              }
            }
          }
        }
      }
    },
    "/api/v1/admin/users": {
      "get": {
        "tags": [
          "管理员管理"
        ],
        "summary": "获取全部用户列表",
        "description": "获取所有用户列表，支持分页",
        "operationId": "getAllUsers",
        "parameters": [
          {
            "name": "page",
            "in": "query",
            "description": "页码，从1开始",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 1
            },
            "example": 1
          },
          {
            "name": "size",
            "in": "query",
            "description": "每页数量",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 10
            },
            "example": 10
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseListUserInfoDTO"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/admin/users/pending": {
      "get": {
        "tags": [
          "管理员管理"
        ],
        "summary": "获取待审核用户列表",
        "description": "获取所有待审核的用户列表，支持分页",
        "operationId": "getPendingUsers",
        "parameters": [
          {
            "name": "page",
            "in": "query",
            "description": "页码，从1开始",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 1
            },
            "example": 1
          },
          {
            "name": "size",
            "in": "query",
            "description": "每页数量",
            "required": false,
            "schema": {
              "type": "integer",
              "format": "int32",
              "default": 10
            },
            "example": 10
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseListUserInfoDTO"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/comments/{id}": {
      "delete": {
        "tags": [
          "评论管理"
        ],
        "summary": "删除评论",
        "description": "逻辑删除评论，只有评论者本人可以删除",
        "operationId": "deleteComment",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "评论ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseVoid"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/admin/posts/{id}": {
      "delete": {
        "tags": [
          "管理员管理"
        ],
        "summary": "强制删除帖子",
        "description": "管理员强制删除帖子（逻辑删除）",
        "operationId": "forceDeletePost",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "帖子ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseVoid"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    },
    "/api/v1/admin/comments/{id}": {
      "delete": {
        "tags": [
          "管理员管理"
        ],
        "summary": "删除评论",
        "description": "管理员删除用户评论（逻辑删除）",
        "operationId": "deleteComment_1",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "评论ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            },
            "example": 1
          }
        ],
        "responses": {
          "200": {
            "description": "OK",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/ApiResponseVoid"
                }
              }
            }
          }
        },
        "security": [
          {
            "Bearer Authentication": []
          }
        ]
      }
    }
  },
  "components": {
    "schemas": {
      "UserUpdateDTO": {
        "type": "object",
        "properties": {
          "nickname": {
            "maxLength": 50,
            "minLength": 2,
            "type": "string",
            "description": "昵称",
            "example": "李四"
          },
          "studentId": {
            "type": "string",
            "description": "学号",
            "example": "2021001001"
          },
          "campusCardUrl": {
            "type": "string",
            "description": "校园卡照片URL",
            "example": "https://example.com/card.jpg"
          }
        },
        "description": "用户信息更新请求"
      },
      "ApiResponseVoid": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "description": "响应码，200表示成功",
            "format": "int32"
          },
          "message": {
            "type": "string",
            "description": "响应消息"
          },
          "data": {
            "type": "object",
            "description": "响应数据"
          }
        },
        "description": "统一API响应"
      },
      "PostCreateDTO": {
        "required": [
          "categoryId",
          "content",
          "title"
        ],
        "type": "object",
        "properties": {
          "title": {
            "maxLength": 20,
            "minLength": 4,
            "type": "string",
            "description": "标题",
            "example": "求购二手自行车"
          },
          "content": {
            "type": "string",
            "description": "帖子内容",
            "example": "想买一辆二手自行车，价格面议"
          },
          "categoryId": {
            "type": "integer",
            "description": "分类ID",
            "format": "int32",
            "example": 1
          },
          "contactInfo": {
            "type": "string",
            "description": "联系方式",
            "example": "微信：xxx"
          }
        },
        "description": "创建帖子请求"
      },
      "ApiResponseLong": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "description": "响应码，200表示成功",
            "format": "int32"
          },
          "message": {
            "type": "string",
            "description": "响应消息"
          },
          "data": {
            "type": "integer",
            "description": "响应数据",
            "format": "int64"
          }
        },
        "description": "统一API响应"
      },
      "ApiResponseBoolean": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "description": "响应码，200表示成功",
            "format": "int32"
          },
          "message": {
            "type": "string",
            "description": "响应消息"
          },
          "data": {
            "type": "boolean",
            "description": "响应数据"
          }
        },
        "description": "统一API响应"
      },
      "CommentCreateDTO": {
        "required": [
          "content",
          "postId"
        ],
        "type": "object",
        "properties": {
          "postId": {
            "type": "integer",
            "description": "帖子ID",
            "format": "int64",
            "example": 1
          },
          "content": {
            "type": "string",
            "description": "评论内容",
            "example": "这个不错！"
          },
          "parentId": {
            "type": "integer",
            "description": "父评论ID，回复评论时使用",
            "format": "int64",
            "example": 1
          }
        },
        "description": "创建评论请求"
      },
      "RegisterDTO": {
        "required": [
          "nickname",
          "password",
          "phone",
          "verifyCode"
        ],
        "type": "object",
        "properties": {
          "phone": {
            "pattern": "^1[3-9]\\d{9}$",
            "type": "string",
            "description": "手机号",
            "example": "13800138000"
          },
          "verifyCode": {
            "maxLength": 6,
            "minLength": 4,
            "type": "string",
            "description": "手机验证码",
            "example": "123456"
          },
          "nickname": {
            "maxLength": 50,
            "minLength": 2,
            "type": "string",
            "description": "昵称",
            "example": "张三"
          },
          "password": {
            "maxLength": 20,
            "minLength": 6,
            "type": "string",
            "description": "密码",
            "example": "123456"
          },
          "studentId": {
            "type": "string",
            "description": "学号",
            "example": "2021001001"
          },
          "campusCardUrl": {
            "type": "string",
            "description": "校园卡照片URL",
            "example": "https://example.com/card.jpg"
          }
        },
        "description": "用户注册请求"
      },
      "LoginDTO": {
        "required": [
          "password",
          "phone"
        ],
        "type": "object",
        "properties": {
          "phone": {
            "type": "string",
            "description": "手机号",
            "example": "13800138000"
          },
          "password": {
            "type": "string",
            "description": "密码",
            "example": "123456"
          }
        },
        "description": "用户登录请求"
      },
      "ApiResponseLoginResponseDTO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "description": "响应码，200表示成功",
            "format": "int32"
          },
          "message": {
            "type": "string",
            "description": "响应消息"
          },
          "data": {
            "$ref": "#/components/schemas/LoginResponseDTO"
          }
        },
        "description": "统一API响应"
      },
      "LoginResponseDTO": {
        "type": "object",
        "properties": {
          "token": {
            "type": "string",
            "description": "JWT Token",
            "example": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
          },
          "userId": {
            "type": "integer",
            "description": "用户ID",
            "format": "int64",
            "example": 1
          },
          "nickname": {
            "type": "string",
            "description": "昵称",
            "example": "张三"
          },
          "role": {
            "type": "string",
            "description": "角色",
            "example": "STUDENT"
          }
        },
        "description": "登录响应"
      },
      "UserMuteDTO": {
        "required": [
          "isMuted",
          "userId"
        ],
        "type": "object",
        "properties": {
          "userId": {
            "type": "integer",
            "description": "用户ID",
            "format": "int64",
            "example": 1
          },
          "isMuted": {
            "type": "boolean",
            "description": "是否禁言：true(禁言)/false(解禁)",
            "example": true
          }
        },
        "description": "用户禁言请求"
      },
      "AuthReviewDTO": {
        "required": [
          "authStatus",
          "userId"
        ],
        "type": "object",
        "properties": {
          "userId": {
            "type": "integer",
            "description": "用户ID",
            "format": "int64",
            "example": 1
          },
          "authStatus": {
            "type": "string",
            "description": "审核结果：approved(通过)/rejected(拒绝)",
            "example": "approved"
          }
        },
        "description": "审核用户注册请求"
      },
      "ApiResponseUserInfoDTO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "description": "响应码，200表示成功",
            "format": "int32"
          },
          "message": {
            "type": "string",
            "description": "响应消息"
          },
          "data": {
            "$ref": "#/components/schemas/UserInfoDTO"
          }
        },
        "description": "统一API响应"
      },
      "UserInfoDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "用户ID",
            "format": "int64"
          },
          "phone": {
            "type": "string",
            "description": "手机号"
          },
          "nickname": {
            "type": "string",
            "description": "昵称"
          },
          "studentId": {
            "type": "string",
            "description": "学号"
          },
          "campusCardUrl": {
            "type": "string",
            "description": "校园卡照片URL"
          },
          "authStatus": {
            "type": "string",
            "description": "认证状态"
          },
          "role": {
            "type": "string",
            "description": "角色"
          },
          "isMuted": {
            "type": "boolean",
            "description": "是否被禁言"
          },
          "createTime": {
            "type": "string",
            "description": "创建时间",
            "format": "date-time"
          }
        },
        "description": "用户信息"
      },
      "ApiResponseListPostListDTO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "description": "响应码，200表示成功",
            "format": "int32"
          },
          "message": {
            "type": "string",
            "description": "响应消息"
          },
          "data": {
            "type": "array",
            "description": "响应数据",
            "items": {
              "$ref": "#/components/schemas/PostListDTO"
            }
          }
        },
        "description": "统一API响应"
      },
      "PostListDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "帖子ID",
            "format": "int64"
          },
          "title": {
            "type": "string",
            "description": "标题"
          },
          "contentSummary": {
            "type": "string",
            "description": "内容摘要（前100字符）"
          },
          "categoryId": {
            "type": "integer",
            "description": "分类ID",
            "format": "int32"
          },
          "categoryName": {
            "type": "string",
            "description": "分类名称"
          },
          "authorId": {
            "type": "integer",
            "description": "发布者ID",
            "format": "int64"
          },
          "authorNickname": {
            "type": "string",
            "description": "发布者昵称"
          },
          "viewCount": {
            "type": "integer",
            "description": "浏览量",
            "format": "int32"
          },
          "likeCount": {
            "type": "integer",
            "description": "点赞数",
            "format": "int32"
          },
          "hotScore": {
            "type": "number",
            "description": "热度值"
          },
          "createTime": {
            "type": "string",
            "description": "创建时间",
            "format": "date-time"
          }
        },
        "description": "帖子列表项"
      },
      "ApiResponsePostDetailDTO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "description": "响应码，200表示成功",
            "format": "int32"
          },
          "message": {
            "type": "string",
            "description": "响应消息"
          },
          "data": {
            "$ref": "#/components/schemas/PostDetailDTO"
          }
        },
        "description": "统一API响应"
      },
      "CommentDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "评论ID",
            "format": "int64"
          },
          "userId": {
            "type": "integer",
            "description": "评论者ID",
            "format": "int64"
          },
          "userNickname": {
            "type": "string",
            "description": "评论者昵称"
          },
          "content": {
            "type": "string",
            "description": "评论内容"
          },
          "parentId": {
            "type": "integer",
            "description": "父评论ID，NULL表示直接评论帖子",
            "format": "int64"
          },
          "parentUserNickname": {
            "type": "string",
            "description": "父评论用户昵称"
          },
          "likeCount": {
            "type": "integer",
            "description": "点赞数",
            "format": "int32"
          },
          "isLiked": {
            "type": "boolean",
            "description": "是否已点赞"
          },
          "createTime": {
            "type": "string",
            "description": "创建时间",
            "format": "date-time"
          },
          "replies": {
            "type": "array",
            "description": "子评论列表",
            "items": {
              "$ref": "#/components/schemas/CommentDTO"
            }
          }
        },
        "description": "评论信息"
      },
      "PostDetailDTO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "帖子ID",
            "format": "int64"
          },
          "title": {
            "type": "string",
            "description": "标题"
          },
          "content": {
            "type": "string",
            "description": "内容"
          },
          "categoryId": {
            "type": "integer",
            "description": "分类ID",
            "format": "int32"
          },
          "categoryName": {
            "type": "string",
            "description": "分类名称"
          },
          "authorId": {
            "type": "integer",
            "description": "发布者ID",
            "format": "int64"
          },
          "authorNickname": {
            "type": "string",
            "description": "发布者昵称"
          },
          "contactInfo": {
            "type": "string",
            "description": "联系方式"
          },
          "viewCount": {
            "type": "integer",
            "description": "浏览量",
            "format": "int32"
          },
          "likeCount": {
            "type": "integer",
            "description": "点赞数",
            "format": "int32"
          },
          "hotScore": {
            "type": "number",
            "description": "热度值"
          },
          "isLiked": {
            "type": "boolean",
            "description": "是否已点赞"
          },
          "createTime": {
            "type": "string",
            "description": "创建时间",
            "format": "date-time"
          },
          "comments": {
            "type": "array",
            "description": "评论列表",
            "items": {
              "$ref": "#/components/schemas/CommentDTO"
            }
          }
        },
        "description": "帖子详情"
      },
      "ApiResponseListUserInfoDTO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "description": "响应码，200表示成功",
            "format": "int32"
          },
          "message": {
            "type": "string",
            "description": "响应消息"
          },
          "data": {
            "type": "array",
            "description": "响应数据",
            "items": {
              "$ref": "#/components/schemas/UserInfoDTO"
            }
          }
        },
        "description": "统一API响应"
      }
    },
    "securitySchemes": {
      "Bearer Authentication": {
        "type": "http",
        "description": "JWT Token认证，格式：Bearer {token}",
        "scheme": "bearer",
        "bearerFormat": "JWT"
      }
    }
  }
}