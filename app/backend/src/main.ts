import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ValidationPipe } from '@nestjs/common';

async function bootstrap() {
    const app = await NestFactory.create(AppModule);
    app.enableCors(); // Allow all CORS for MVP
    app.useGlobalPipes(new ValidationPipe());

    // Swagger Configuration
    const { DocumentBuilder, SwaggerModule } = await import('@nestjs/swagger');
    const config = new DocumentBuilder()
        .setTitle('FileShare API')
        .setDescription('API documentation for FileShare mobile app')
        .setVersion('1.0')
        .addBearerAuth()
        .build();
    const document = SwaggerModule.createDocument(app, config);
    SwaggerModule.setup('api/docs', app, document);

    await app.listen(process.env.APP_PORT || 3001);
}
bootstrap();
