import { Body, Controller, Get, Post, Patch, Param, UseGuards, BadRequestException, Delete } from '@nestjs/common';
import { UsersService } from './users.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { UserRole } from './user.entity';
import { RolesGuard } from '../auth/roles.guard';
import { Roles } from '../auth/roles.decorator';
import * as bcrypt from 'bcrypt';

@Controller('users')
@UseGuards(JwtAuthGuard, RolesGuard)
export class UsersController {
    constructor(private readonly usersService: UsersService) { }

    @Get()
    @Roles(UserRole.ADMIN)
    findAll() {
        return this.usersService.findAll();
    }

    @Post()
    @Roles(UserRole.ADMIN)
    async create(
        @Body()
        body: {
            username: string;
            email?: string;
            password?: string;
            role?: UserRole;
        },
    ) {
        if (!body.username) {
            throw new BadRequestException('username is required');
        }
        return this.usersService.create(body);
    }

    @Patch(':id')
    @Roles(UserRole.ADMIN)
    async update(@Param('id') id: string, @Body() body: any) {
        // Password Reset Handling
        if (body.password) {
            const salt = await bcrypt.genSalt();
            body.password_hash = await bcrypt.hash(body.password, salt);
            delete body.password;
        }
        return this.usersService.update(id, body);
    }
}




