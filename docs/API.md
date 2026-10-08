Пользователи и вход

POST /api/auth/register - регистрация пользователя
Запрос:

```JSON
{
	"phone/email": str,
	"login": str,
	"password": str
}
```


Ответ:

```JSON
{
	"error_status": int,
	"JWT": str
}
```


POST /api/auth/login - вход пользователя
Запрос:

```JSON
{
	"phone/email/login": str,
	"password": str
}
```


Ответ:

```JSON
{
	"error_status": int,
	"JWT": str
}
```



POST /api/auth/forgot-password/request - отправление запроса на восстановление пароля

Запрос:

```JSON
{
    "nickname": str,
    "phone": str,
    "email": str
}
```

Ответ:

```JSON
{
    "status": int,
    "code_token": str
}
```

POST /api/auth/forgot-password/code - отправка кода, который был получен на номер телефона/почту

Запрос:

```JSON
{
    "code_token": str,
    "verify_code": str
}
```

Ответ:

```JSON
{
    "reset_password_token": str
}
```

POST /api/auth/forgot-password/reset- отправка кода, который был получен на номер телефона/почту

Запрос:

```JSON
{
    "reset_password_token": str,
    "new_password": str
}
```

Ответ:

```JSON
{
    "status": str
}
```

GET /api/users/me - получение информации о себе
Запрос:

```JSON
{}
```


Ответ:

```JSON
{
	"nickname": str,
	"phone": str,
	"email": str
}
```


PUT /api/users/me - изменение информации о себе
Запрос:

```JSON
{
	"nickname": str,
	"phone": str,
	"email": str
}
```


Ответ:

```JSON
{
	"error_status": int
}
```


GET /api/users/me/balance - получение баланса
Запрос:

```JSON
{}
```


Ответ:

```JSON
{
	"balance": int
}
```


GET /api/users/me/inventory - получение купленных продуктов
Запрос:

```JSON
{}
```


Ответ:

```JSON
[
	{
		"id": str,
		"amount": str
	},
	{...}
]
```


GET /api/users/me/transactions - получение истории операции пользователя
Запрос:

```JSON
{}
```


Ответ:


```JSON
[
	{
		"amount": int,
		"item": 
		{
			"partner": str,
			"price": int
		},
		"completed_task":
		{
			"id": int
		}
	}
]
```


Анкетирование

GET /api/questionnaire - получение вопросов анкеты
Запрос:

```JSON
{}
```


Ответ:

```JSON
[
	{
		"questionId": int,
		"question": str,
		"answers":
		[
			str,
			...
		]
	},
	...
]
```


PUT /api/users/me/questionnaire - изменение ответов на анкету
Запрос:

```JSON
[
	{
		"questionId": int,
		"answer": str
	},
	...
]
```


Ответ:

```JSON
{
	"error_status": int
}
```


GET /api/users/me/questionnaire - получение ответов на анкету
Запрос:

```JSON
{}
```


Ответ:

```JSON
[
	{
		"questionId": int,
		"answer": str
	},
	...
]
```


Тренировки

GET /api/users/me/tasks - получение всех заданий
Запрос:

```JSON
{}
```


Ответ:

```JSON
[
	{
		"id": int,
		"exercises":
		[
			{
				"id": int,
				"name": str,
				"description": str
			}
		]
	}
]
```


GET /api/users/me/tasks/{id} - получение информации о задании с номером id
Запрос:

```JSON
{}
```


Ответ:

```JSON
[
	{
		"id": int,
		"exercises":
		[
			{
				"id": int,
				"name": str,
				"description": str
			},
			...
		]
	}
]
```


GET /api/users/me/tasks/{id}/exercises - получение информации о упражнениях из задания
Запрос:

```JSON
{}
```


Ответ:

```JSON
[
	{
		"id": int,
		"name": str,
		"description": str
	},
	...
]
```


GET /api/users/me/tasks/{id}/exercises/{id} - получение информации о конкретном упражнении из задания
Запрос:

```JSON
{}
```


Ответ:

```JSON
[
	{
		"id": int,
		"name": str,
		"description": str
	}
]
```


GET /api/users/me/tasks/{id}/progress - получение прогресса выполнения задания
Запрос:

```JSON
{}
```


Ответ:

```JSON
{
	"progress": int
}
```


PUT /api/users/me/tasks/{id}/progress - обновление прогресса выполнения задания
Запрос:

```JSON
{
	"progress": int
}
```


Ответ:

```JSON
{}
```


Магазин

GET /api/shop - получение списка всех доступных товаров
Запрос:

```JSON
{}
```


Ответ:

```JSON
[
	{
		"partner": int,
		"price": int
	},
	...
]
```


GET /api/shop/{id} - получение информации о товаре
Запрос:

```JSON
{}
```


Ответ:

```JSON
{
	"id": int,
	"partner": int,
	"price": int
}
```


POST /api/shop/{id}/purchase - покупка предмета
Запрос:

```JSON
{}
```


Ответ:

```JSON
{
	"error_status": int
}
```


Партнёры

GET /api/partners - получение всех партнёров
Запрос:

```JSON
{}
```


Ответ:

```JSON
{
	"id": int
}
```


GET /api/partners/{id} - получение информации о партнёре по id
Запрос:

```JSON
{}
```


Ответ:

```JSON
{
	"id": int
}
```