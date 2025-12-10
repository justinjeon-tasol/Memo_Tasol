import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Attachment } from './attachment.entity';
import { ConfigService } from '@nestjs/config';
import * as fs from 'fs';
import * as path from 'path';

@Injectable()
export class AttachmentsService {
    constructor(
        @InjectRepository(Attachment)
        private attachmentsRepository: Repository<Attachment>,
        private configService: ConfigService,
    ) { }

    async create(file: Express.Multer.File, itemId: string, userId: string) {
        // Thumbnail generation if image
        if (file.mimetype.startsWith('image/')) {
            try {
                // Dynamic import for sharp to avoid issues if not installed or platform specific
                const sharp = await import('sharp');
                const thumbPath = file.path.replace(path.extname(file.path), '_thumb.jpg');

                await sharp.default(file.path)
                    .resize(300, 300, { fit: 'inside' })
                    .toFormat('jpeg')
                    .toFile(thumbPath);
            } catch (error) {
                console.error('Thumbnail generation failed', error);
            }
        }

        const attachment = this.attachmentsRepository.create({
            item_id: itemId,
            file_name: file.originalname,
            file_path: file.path,
            file_size: file.size,
            mime_type: file.mimetype,
            uploaded_by_id: userId,
        });

        // Adjust file_path to be relative if needed, but for now we store what multer gives us or relative to FILES_BASE_PATH
        // If multer stores absolute path, we might want to strip the base path for portability.
        // For this MVP, let's store the relative path from the upload root.
        // For this MVP, let's store the relative path from the upload root.
        const basePath = this.configService.get<string>('FILES_BASE_PATH') || './uploads';
        if (file.path.startsWith(basePath)) {
            attachment.file_path = path.relative(basePath, file.path);
        }

        return this.attachmentsRepository.save(attachment);
    }

    async findAllByItem(itemId: string) {
        return this.attachmentsRepository.find({ where: { item_id: itemId }, order: { uploaded_at: 'DESC' } });
    }

    async findOne(id: string) {
        return this.attachmentsRepository.findOne({ where: { id } });
    }

    getFilePath(attachment: Attachment): string {
        const basePath = this.configService.get<string>('FILES_BASE_PATH') || './uploads';
        return path.join(basePath, attachment.file_path);
    }
}
