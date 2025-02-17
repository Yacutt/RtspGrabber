# RtspGrabber: Сервис обработки RTSP-видеопотока

Проект состоит из двух сервисов, предназначенных для обработки RTSP-видеопотока и предоставления информации о них в формате JSON.

## Описание сервисов

**1. Processing Service:**  
    •   Принимает на вход RTSP-видеопоток.  
    •   Разбивает видеопоток на отдельные кадры.  
    •   Кодирует каждый кадр в формат Base64.  
    •   Отправляет закодированный кадр и временную метку во второй сервис (Target Service).  
    •   Получает от Target Service ответ в виде JSON-строки следующего формата:  
  ```json
  {  
    "timestamp": "<временная метка>",  
    "imageWidth": "<ширина изображения>",  
    "imageHeight": "<высота изображения>",  
    "imageType": "<тип изображения>",  
    "message": "Frame processed successfully"  
  }  
  ```  
•  Записывает этот ответ в ./output/output.json
  
**2. Target Service:**  
    •   Принимает строку  от Processing Service, содержащую закодированный Base64 кадр и временную метку.  
    •   Декодирует Base64 кадр обратно в изображение.  
    •   Формирует ответ в виде JSON-строки формата описанного выше  
    •   Отправляет подтверждение (JSON-строку).

## Cборка и Запуск

Оба сервиса используют Maven для управления зависимостями и сборки.

**Запуск**  

**1.** Клонируйте репозиторий и перейдите в корень Target Service:
```bash
  git clone https://github.com/Yacutt/RtspGrabber
  cd  RtspGrabber/targetService/receiving    
```

**2.** Соберите проект с помощью Maven:
```bash
  mvn clean install
```  
   Эта команда скомпилирует код и создаст исполняемый JAR-файл в директории target.

**3.** Перейдите в директорию target и запустите сервис:  
```bash
  cd target
  java -jar target-service-0.0.1.jar
```

**4.** Затем перейдите в корень Proccessing Service и соберите проект с помощью Maven:
```bash
  cd  RtspGrabber/processingService/processing
  mvn clean install
```

**5.** Перейдите в директорию target и запустите сервис:  
```bash
  cd target
  java -jar processing-service-0.0.1.jar
```

## Конфигурация

### Processing Service

В файле processingService/processing/src/main/resources/application.properties вы можете настроить проект "под себя":  
•  изменить порт (используется 8081)  
•  изменить rtsp-видеопоток (используется rtsp://37.230.146.42:554)  
•  изменить директорию и файл, для сохранения результатов (сейчас ./output/output.json, если директория или файл отсутствуют, они будут созданы в корне проекта автоматически)  

### Target Service

В файле targetService/receiving/src/main/resources/application.properties вы можете изменить порт, на котором будет запущен сервис (используется 8082)
