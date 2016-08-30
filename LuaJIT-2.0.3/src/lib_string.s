	.arch armv5te
	.fpu softvfp
	.eabi_attribute 20, 1
	.eabi_attribute 21, 1
	.eabi_attribute 23, 3
	.eabi_attribute 24, 1
	.eabi_attribute 25, 1
	.eabi_attribute 26, 2
	.eabi_attribute 30, 2
	.eabi_attribute 34, 0
	.eabi_attribute 18, 4
	.file	"lib_string.c"
	.text
	.align	2
	.type	lj_ffh_string_len, %function
lj_ffh_string_len:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r3, lr}
	mov	r1, #1
	bl	lj_lib_checkstr(PLT)
	mov	r0, #0
	ldmfd	sp!, {r3, pc}
	.size	lj_ffh_string_len, .-lj_ffh_string_len
	.align	2
	.type	writer_buf, %function
writer_buf:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	mov	r0, r3
	stmfd	sp!, {r3, lr}
	bl	luaL_addlstring(PLT)
	mov	r0, #0
	ldmfd	sp!, {r3, pc}
	.size	writer_buf, .-writer_buf
	.align	2
	.type	addintlen, %function
addintlen:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, r5, r6, lr}
	mov	r4, r0
	bl	strlen(PLT)
	ldr	r1, .L7
	mov	r2, #2
.LPIC0:
	add	r1, pc, r1
	sub	r3, r0, #1
	mov	r6, r0
	add	r0, r4, r3
	ldrb	r5, [r4, r3]	@ zero_extendqisi2
	bl	memcpy(PLT)
	mov	r3, #0
	strb	r5, [r4, r6]!
	strb	r3, [r4, #1]
	ldmfd	sp!, {r4, r5, r6, pc}
.L8:
	.align	2
.L7:
	.word	.LC0-(.LPIC0+8)
	.size	addintlen, .-addintlen
	.align	2
	.type	lj_cf_string_format, %function
lj_cf_string_format:
	@ args = 0, pretend = 0, frame = 1648
	@ frame_needed = 0, uses_anonymous_args = 0
	ldr	r3, [r0, #20]
	ldr	ip, [r0, #16]
	stmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, lr}
	rsb	ip, ip, r3
	sub	sp, sp, #1648
	sub	sp, sp, #4
	mov	r3, ip, asr #3
	mov	r1, #1
	mov	fp, r0
	str	r3, [sp, #4]
	bl	lj_lib_checkstr(PLT)
	add	r7, sp, #624
	sub	r1, r7, #12
	str	r1, [sp, #16]
	ldr	r5, [r0, #12]
	add	r4, r0, #16
	add	r5, r4, r5
	mov	r0, fp
	bl	luaL_buffinit(PLT)
	cmp	r4, r5
	bcs	.L71
	ldr	r0, .L136
	ldr	r2, .L136+4
.LPIC11:
	add	r0, pc, r0
	ldr	r8, .L136+8
	add	r3, r0, #1
.LPIC12:
	add	r2, pc, r2
.LPIC16:
	add	r8, pc, r8
	str	r3, [sp, #8]
	add	r3, r2, #1
	str	r3, [sp, #12]
	add	r3, r8, #1
	str	r3, [sp, #20]
	mov	r8, #1
	str	fp, [sp]
	ldr	r9, [sp, #16]
	b	.L70
.L113:
	ldr	r3, [sp, #612]
	add	r2, r7, #1024
	cmp	r3, r2
	bcs	.L112
.L12:
	add	r2, r3, #1
	str	r2, [sp, #612]
	add	r4, r4, #1
	ldrb	r2, [r4, #-1]	@ zero_extendqisi2
	strb	r2, [r3]
.L13:
	cmp	r4, r5
	bcs	.L71
.L70:
	ldrb	r3, [r4]	@ zero_extendqisi2
	cmp	r3, #37
	bne	.L113
	ldrb	r3, [r4, #1]	@ zero_extendqisi2
	cmp	r3, #37
	beq	.L114
	ldr	r2, [sp, #4]
	add	r8, r8, #1
	cmp	r2, r8
	blt	.L115
.L16:
	cmp	r3, #0
	add	r4, r4, #1
	moveq	fp, r3
	moveq	r10, r4
	beq	.L18
	ldr	r6, .L136+12
	mov	fp, r3
.LPIC10:
	add	r6, pc, r6
	mov	r10, r4
	b	.L19
.L21:
	ldrb	fp, [r10, #1]!	@ zero_extendqisi2
	cmp	fp, #0
	beq	.L103
.L19:
	mov	r0, r6
	mov	r1, fp
	bl	strchr(PLT)
	cmp	r0, #0
	bne	.L21
.L103:
	rsb	r2, r4, r10
	cmp	r2, #5
	bhi	.L116
.L18:
	ldr	r3, [sp, #8]
	ldr	r2, [sp, #12]
	ldrb	r3, [fp, r3]	@ zero_extendqisi2
	tst	r3, #8
	addne	r10, r10, #1
	ldrb	r3, [r10]	@ zero_extendqisi2
	ldrb	r2, [r3, r2]	@ zero_extendqisi2
	tst	r2, #8
	ldrneb	r3, [r10, #1]	@ zero_extendqisi2
	addne	r10, r10, #1
	cmp	r3, #46
	beq	.L117
.L24:
	ldr	r2, .L136+16
.LPIC15:
	add	r2, pc, r2
	add	r2, r2, #1
	ldrb	r3, [r3, r2]	@ zero_extendqisi2
	tst	r3, #8
	bne	.L118
.L26:
	rsb	fp, r4, r10
	mov	r2, #37
	add	r6, sp, #1648
	strb	r2, [r6, #-1600]!
	mov	r1, r4
	add	r2, fp, #1
	add	r0, r6, #1
	bl	strncpy(PLT)
	add	r3, sp, #1648
	add	r3, r3, fp
	mov	r2, #0
	strb	r2, [r3, #-1598]
	ldrb	r2, [r10]	@ zero_extendqisi2
	add	r4, r10, #1
	sub	r3, r2, #65
	cmp	r3, #55
	addls	pc, pc, r3, asl #2
	b	.L27
.L29:
	b	.L28
	b	.L27
	b	.L27
	b	.L27
	b	.L28
	b	.L27
	b	.L28
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L30
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L28
	b	.L27
	b	.L31
	b	.L30
	b	.L28
	b	.L28
	b	.L28
	b	.L27
	b	.L30
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L27
	b	.L30
	b	.L33
	b	.L34
	b	.L27
	b	.L35
	b	.L27
	b	.L30
	b	.L27
	b	.L27
	b	.L30
	.p2align 1
.L30:
	mov	r0, r6
	bl	addintlen(PLT)
	ldr	r0, [sp]
	mov	r1, r8
	bl	lj_lib_checkbit(PLT)
.L105:
	add	r10, sp, #100
	mov	r2, r0
	mov	r1, r6
	mov	r0, r10
	bl	sprintf(PLT)
.L36:
	mov	r0, r10
	bl	strlen(PLT)
	mov	r1, r10
	mov	r2, r0
	mov	r0, r9
	bl	luaL_addlstring(PLT)
	cmp	r4, r5
	bcc	.L70
.L71:
	ldr	r0, [sp, #16]
	bl	luaL_pushresult(PLT)
	mov	r0, #1
	add	sp, sp, #1648
	add	sp, sp, #4
	@ sp needed
	ldmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, pc}
.L112:
	mov	r0, r9
	bl	luaL_prepbuffer(PLT)
	ldr	r3, [sp, #612]
	b	.L12
.L114:
	ldr	r3, [sp, #612]
	add	r2, r7, #1024
	cmp	r3, r2
	bcs	.L119
.L15:
	add	r2, r3, #1
	str	r2, [sp, #612]
	ldrb	r2, [r4, #1]	@ zero_extendqisi2
	strb	r2, [r3]
	add	r4, r4, #2
	b	.L13
.L115:
	ldr	r3, .L136+20
	ldr	r0, [sp]
.LPIC9:
	add	r3, pc, r3
	ldr	r2, [r3]
	mov	r1, r8
	bl	luaL_argerror(PLT)
	ldrb	r3, [r4, #1]	@ zero_extendqisi2
	b	.L16
.L117:
	ldr	r3, .L136+24
	ldrb	r2, [r10, #1]	@ zero_extendqisi2
.LPIC13:
	add	r3, pc, r3
	add	r3, r3, #1
	ldrb	r2, [r2, r3]	@ zero_extendqisi2
	tst	r2, #8
	addeq	r10, r10, #1
	beq	.L26
	ldrb	r2, [r10, #2]	@ zero_extendqisi2
	add	r1, r10, #2
	ldrb	r3, [r2, r3]	@ zero_extendqisi2
	tst	r3, #8
	beq	.L80
	ldrb	r3, [r10, #3]	@ zero_extendqisi2
	add	r10, r10, #3
	b	.L24
.L34:
	ldr	r0, [sp]
	mov	r1, r8
	bl	lj_lib_checkstr(PLT)
	ldr	r3, [sp, #612]
	add	r2, r7, #1024
	cmp	r3, r2
	ldr	r10, [r0, #12]
	add	r6, r0, #16
	bcs	.L120
.L42:
	add	r1, r3, #1
	mov	r2, #34
	cmp	r10, #0
	str	r1, [sp, #612]
	strb	r2, [r3]
	beq	.L57
	ldr	r0, .L136+28
	mov	r3, r10
.LPIC17:
	add	r0, pc, r0
	add	r10, r7, #1024
	ldr	fp, [sp, #20]
	add	r1, r0, #1
	str	r4, [sp, #28]
	str	r5, [sp, #32]
	mov	r4, r6
	mov	r5, r10
	mov	r10, r3
	str	r1, [sp, #24]
.L56:
	ldrb	ip, [r4], #1	@ zero_extendqisi2
	cmp	ip, #34
	cmpne	ip, #92
	moveq	r1, #1
	movne	r1, #0
	cmp	ip, #10
	orreq	r1, r1, #1
	cmp	r1, #0
	mov	r6, ip
	bne	.L121
	ldrb	r1, [fp, ip]	@ zero_extendqisi2
	tst	r1, #1
	beq	.L48
	ldr	r1, [sp, #612]
	cmp	r1, r5
	bcs	.L122
.L49:
	add	r0, r1, #1
	mov	r3, #92
	cmp	ip, #99
	str	r0, [sp, #612]
	strb	r3, [r1]
	bhi	.L50
	ldrb	r1, [r4]	@ zero_extendqisi2
	ldr	r3, [sp, #24]
	ldrb	r1, [r1, r3]	@ zero_extendqisi2
	tst	r1, #8
	beq	.L123
	ldr	r1, [sp, #612]
	add	r3, sp, #1648
	cmp	r1, r3
	bcs	.L124
.L106:
	add	r0, r1, #1
	str	r0, [sp, #612]
	mov	r0, #48
	strb	r0, [r1]
	b	.L53
.L33:
	ldr	r6, [sp]
	mov	r1, r8
	mov	r0, r6
	bl	lua_topointer(PLT)
	ldr	r1, .L136+32
.LPIC18:
	add	r1, pc, r1
	mov	r2, r0
	mov	r0, r6
	bl	lj_str_pushf(PLT)
	mov	r0, r9
	bl	luaL_addvalue(PLT)
	b	.L13
.L35:
	ldr	r3, [sp]
	sub	r2, r8, #-536870911
	ldr	r10, [r3, #16]
	mov	ip, r2, asl #3
	add	r3, r10, ip
	ldr	r1, [r3, #4]
	cmn	r1, #5
	bne	.L59
	ldr	r2, [r10, r2, asl #3]
.L60:
	mov	r0, r6
	mov	r1, #46
	str	r2, [sp, #24]
	bl	strchr(PLT)
	ldr	r2, [sp, #24]
	cmp	r0, #0
	beq	.L125
.L69:
	add	r10, sp, #100
	mov	r1, r6
	add	r2, r2, #16
	mov	r0, r10
	bl	sprintf(PLT)
	b	.L36
.L31:
	ldr	r0, [sp]
	mov	r1, r8
	bl	lj_lib_checkint(PLT)
	b	.L105
.L28:
	ldr	r0, [sp]
	mov	r1, r8
	bl	lj_lib_checknum(PLT)
	mov	r3, r1
	mov	r2, r0
	strd	r2, [r6, #-8]
	ldr	r1, [sp, #44]
	mov	r1, r1, asl #1
	cmn	r1, #2097152
	bcs	.L126
	add	r10, sp, #100
	mov	r1, r6
	mov	r0, r10
	bl	sprintf(PLT)
	b	.L36
.L27:
	ldr	fp, [sp]
	mov	r1, #1888
	mov	r0, fp
	bl	lj_err_callerv(PLT)
.L50:
	ldr	r1, [sp, #612]
	add	r3, sp, #1648
	cmp	r1, r3
	addcc	r0, r1, #1
	strcc	r0, [sp, #612]
	bcs	.L127
.L75:
	mov	r0, #49
	sub	r6, ip, #100
	strb	r0, [r1]
.L53:
	mov	r1, #205
	mul	r1, r6, r1
	ldr	r0, [sp, #612]
	mov	r1, r1, lsr #11
	add	ip, r1, r1, asl #2
	cmp	r0, r5
	sub	r6, r6, ip, asl #1
	bcs	.L128
.L54:
	add	r1, r1, #48
	add	ip, r0, #1
	str	ip, [sp, #612]
	strb	r1, [r0]
.L52:
	add	r6, r6, #48
.L48:
	ldr	r1, [sp, #612]
	cmp	r1, r5
	bcs	.L129
.L55:
	add	r0, r1, #1
	subs	r10, r10, #1
	str	r0, [sp, #612]
	strb	r6, [r1]
	bne	.L56
	ldr	r4, [sp, #28]
	ldr	r5, [sp, #32]
.L57:
	ldr	r3, [sp, #612]
	add	r2, r7, #1024
	cmp	r3, r2
	bcc	.L44
	mov	r0, r9
	bl	luaL_prepbuffer(PLT)
	ldr	r3, [sp, #612]
.L44:
	add	r1, r3, #1
	mov	r2, #34
	str	r1, [sp, #612]
	strb	r2, [r3]
	b	.L13
.L121:
	ldr	r1, [sp, #612]
	cmp	r1, r5
	bcs	.L130
.L47:
	add	r0, r1, #1
	mov	r3, #92
	str	r0, [sp, #612]
	strb	r3, [r1]
	ldr	r1, [sp, #612]
	cmp	r1, r5
	bcc	.L55
.L129:
	mov	r0, r9
	bl	luaL_prepbuffer(PLT)
	ldr	r1, [sp, #612]
	b	.L55
.L130:
	mov	r0, r9
	bl	luaL_prepbuffer(PLT)
	ldr	r1, [sp, #612]
	b	.L47
.L119:
	mov	r0, r9
	bl	luaL_prepbuffer(PLT)
	ldr	r3, [sp, #612]
	b	.L15
.L123:
	cmp	ip, #9
	bls	.L52
	b	.L53
.L122:
	mov	r0, r9
	str	ip, [sp, #36]
	bl	luaL_prepbuffer(PLT)
	ldr	r1, [sp, #612]
	ldr	ip, [sp, #36]
	b	.L49
.L128:
	mov	r0, r9
	str	r1, [sp, #36]
	bl	luaL_prepbuffer(PLT)
	ldr	r0, [sp, #612]
	ldr	r1, [sp, #36]
	b	.L54
.L125:
	ldr	r3, [r2, #12]
	cmp	r3, #99
	bls	.L69
	ldr	ip, [sp]
	mvn	r1, #4
	ldr	r3, [ip, #20]
	add	r0, r3, #8
	str	r0, [ip, #20]
	str	r2, [r3]
	str	r1, [r3, #4]
	mov	r0, r9
	bl	luaL_addvalue(PLT)
	b	.L13
.L120:
	mov	r0, r9
	bl	luaL_prepbuffer(PLT)
	ldr	r3, [sp, #612]
	b	.L42
.L127:
	mov	r0, r9
	str	ip, [sp, #36]
	bl	luaL_prepbuffer(PLT)
	ldr	r1, [sp, #612]
	ldr	ip, [sp, #36]
	add	r0, r1, #1
	str	r0, [sp, #612]
	b	.L75
.L124:
	mov	r0, r9
	bl	luaL_prepbuffer(PLT)
	ldr	r1, [sp, #612]
	b	.L106
.L126:
	add	r2, sp, #68
	mov	r0, r2
	sub	r1, r6, #8
	str	r2, [sp, #24]
	bl	lj_str_bufnum(PLT)
	ldrb	r3, [r10]	@ zero_extendqisi2
	ldr	r2, [sp, #24]
	cmp	r3, #96
	bhi	.L38
	add	r1, sp, #1648
	sub	r3, r0, #3
	add	r3, r1, r3
	ldrb	ip, [r3, #-1580]	@ zero_extendqisi2
	add	lr, sp, #1648
	sub	r1, r0, #2
	add	r1, lr, r1
	sub	ip, ip, #32
	strb	ip, [r3, #-1580]
	ldrb	ip, [r1, #-1580]	@ zero_extendqisi2
	sub	r3, r0, #1
	add	r3, lr, r3
	sub	ip, ip, #32
	strb	ip, [r1, #-1580]
	ldrb	r1, [r3, #-1580]	@ zero_extendqisi2
	sub	r1, r1, #32
	strb	r1, [r3, #-1580]
.L38:
	ldrb	r3, [sp, #48]	@ zero_extendqisi2
	add	r1, sp, #1648
	add	r0, r1, r0
	subs	r1, r3, #46
	movne	r1, #1
	cmp	r3, #64
	movhi	r1, #0
	mov	r3, #0
	cmp	r1, r3
	strb	r3, [r0, #-1580]
	mov	r3, r6
	beq	.L39
.L40:
	ldrb	r1, [r3, #1]!	@ zero_extendqisi2
	subs	r0, r1, #46
	movne	r0, #1
	cmp	r1, #64
	movhi	r0, #0
	cmp	r0, #0
	bne	.L40
.L39:
	add	r10, sp, #100
	mov	lr, #115
	mov	ip, #0
	mov	r1, r6
	mov	r0, r10
	strb	lr, [r3]
	strb	ip, [r3, #1]
	bl	sprintf(PLT)
	b	.L36
.L59:
	ldr	fp, [sp]
	mov	r1, r3
	mov	r2, #18
	mov	r0, fp
	str	r3, [sp, #24]
	str	ip, [sp, #28]
	bl	lj_meta_lookup(PLT)
	ldr	r3, [sp, #24]
	ldr	r2, [r0, #4]
	cmn	r2, #1
	beq	.L61
	ldr	r3, [fp, #20]
	ldrd	r0, [r0]
	ldr	ip, [sp, #28]
	mov	r2, r3
	strd	r0, [r2], #16
	ldrd	r0, [r10, ip]
	mov	lr, fp
	mov	r10, r0
	mov	fp, r1
	mov	r1, #1
	str	r2, [lr, #20]
	mov	r0, lr
	strd	r10, [r3, #8]
	mov	r2, r1
	mov	r10, lr
	str	ip, [sp, #24]
	bl	lua_call(PLT)
	ldr	r3, [r10, #20]
	ldr	ip, [sp, #24]
	sub	r2, r3, #8
	str	r2, [r10, #20]
	ldr	r2, [r3, #-4]
	cmn	r2, #5
	beq	.L104
	ldr	r2, [sp]
	ldrd	r0, [r3, #-8]
	ldr	r2, [r2, #16]
	add	r3, r2, ip
	strd	r0, [r2, ip]
.L61:
	ldr	r1, [r3, #4]
	cmn	r1, #14
	bls	.L131
	cmn	r1, #1
	beq	.L132
	cmn	r1, #2
	beq	.L133
	cmn	r1, #3
	beq	.L134
	cmn	r1, #9
	beq	.L135
.L67:
	ldr	r3, .L136+36
	ldr	fp, [sp]
.LPIC23:
	add	r3, pc, r3
	mvn	r1, r1
	ldr	r10, [r3, r1, asl #2]
	mov	r0, fp
	mov	r1, r8
	bl	lua_topointer(PLT)
	ldr	r1, .L136+40
	mov	r2, r10
.LPIC24:
	add	r1, pc, r1
	mov	r3, r0
	mov	r0, fp
	bl	lj_str_pushf(PLT)
.L68:
	ldr	r1, [sp]
	ldr	r3, [r1, #20]
	sub	r2, r3, #8
	str	r2, [r1, #20]
.L104:
	ldr	r2, [r3, #-8]
	b	.L60
.L135:
	ldr	r3, [r3]
	ldrb	r2, [r3, #6]	@ zero_extendqisi2
	cmp	r2, #1
	bls	.L67
	ldr	r1, .L136+44
	ldr	r0, [sp]
.LPIC22:
	add	r1, pc, r1
	bl	lj_str_pushf(PLT)
	b	.L68
.L131:
	mov	r1, r3
	ldr	r0, [sp]
	bl	lj_str_fromnumber(PLT)
	mov	r2, r0
	b	.L60
.L132:
	ldr	r1, .L136+48
	mov	r2, #3
	ldr	r0, [sp]
.LPIC19:
	add	r1, pc, r1
	bl	lj_str_new(PLT)
	mov	r2, r0
	b	.L60
.L134:
	ldr	r1, .L136+52
	mov	r2, #4
	ldr	r0, [sp]
.LPIC21:
	add	r1, pc, r1
	bl	lj_str_new(PLT)
	mov	r2, r0
	b	.L60
.L133:
	ldr	r1, .L136+56
	mov	r2, #5
	ldr	r0, [sp]
.LPIC20:
	add	r1, pc, r1
	bl	lj_str_new(PLT)
	mov	r2, r0
	b	.L60
.L118:
	ldr	fp, [sp]
	ldr	r1, .L136+60
	mov	r0, fp
	bl	lj_err_caller(PLT)
.L116:
	ldr	fp, [sp]
	ldr	r1, .L136+64
	mov	r0, fp
	bl	lj_err_caller(PLT)
.L80:
	mov	r10, r1
	b	.L26
.L137:
	.align	2
.L136:
	.word	lj_char_bits-(.LPIC11+8)
	.word	lj_char_bits-(.LPIC12+8)
	.word	lj_char_bits-(.LPIC16+8)
	.word	.LC1-(.LPIC10+8)
	.word	lj_char_bits-(.LPIC15+8)
	.word	lj_obj_typename-(.LPIC9+8)
	.word	lj_char_bits-(.LPIC13+8)
	.word	lj_char_bits-(.LPIC17+8)
	.word	.LC2-(.LPIC18+8)
	.word	lj_obj_itypename-(.LPIC23+8)
	.word	.LC7-(.LPIC24+8)
	.word	.LC6-(.LPIC22+8)
	.word	.LC3-(.LPIC19+8)
	.word	.LC5-(.LPIC21+8)
	.word	.LC4-(.LPIC20+8)
	.word	1954
	.word	1922
	.size	lj_cf_string_format, .-lj_cf_string_format
	.align	2
	.type	lj_cf_string_gmatch, %function
lj_cf_string_gmatch:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, r6, r7, lr}
	mov	r1, #1
	mov	r4, r0
	bl	lj_lib_checkstr(PLT)
	mov	r0, r4
	mov	r1, #2
	bl	lj_lib_checkstr(PLT)
	ldr	r3, [r4, #16]
	ldr	r1, .L140
	add	r2, r3, #24
	mov	r6, #0
	mov	r7, #0
	str	r2, [r4, #20]
	mov	r0, r4
	strd	r6, [r3, #16]
.LPIC25:
	add	r1, pc, r1
	mov	r2, #3
	bl	lua_pushcclosure(PLT)
	ldr	r2, [r4, #20]
	mov	r3, #88
	ldr	r2, [r2, #-8]
	mov	r0, #1
	strb	r3, [r2, #6]
	ldr	r3, [r4, #8]
	add	r3, r3, #208
	str	r3, [r2, #16]
	ldmfd	sp!, {r4, r6, r7, pc}
.L141:
	.align	2
.L140:
	.word	lj_cf_string_gmatch_aux-(.LPIC25+8)
	.size	lj_cf_string_gmatch, .-lj_cf_string_gmatch
	.align	2
	.type	lj_cf_string_dump, %function
lj_cf_string_dump:
	@ args = 0, pretend = 0, frame = 1040
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, r5, r6, r7, lr}
	sub	sp, sp, #1040
	mov	r1, #1
	sub	sp, sp, #12
	mov	r4, r0
	bl	lj_lib_checkfunc(PLT)
	ldr	r2, [r4, #16]
	ldr	r1, [r4, #20]
	add	r3, r2, #8
	cmp	r3, r1
	movcs	r6, #0
	mov	r7, r0
	bcs	.L143
	ldr	r6, [r2, #12]
	cmn	r6, #3
	movhi	r6, #0
	movls	r6, #1
.L143:
	add	r5, sp, #12
	str	r3, [r4, #20]
	mov	r1, r5
	mov	r0, r4
	bl	luaL_buffinit(PLT)
	ldrb	r3, [r7, #6]	@ zero_extendqisi2
	cmp	r3, #0
	bne	.L145
	ldr	r1, [r7, #16]
	ldr	r2, .L148
	mov	r0, r4
	sub	r1, r1, #64
	str	r6, [sp]
.LPIC26:
	add	r2, pc, r2
	mov	r3, r5
	bl	lj_bcwrite(PLT)
	cmp	r0, #0
	bne	.L145
	mov	r0, r5
	bl	luaL_pushresult(PLT)
	mov	r0, #1
	add	sp, sp, #1040
	add	sp, sp, #12
	@ sp needed
	ldmfd	sp!, {r4, r5, r6, r7, pc}
.L145:
	mov	r0, r4
	ldr	r1, .L148+4
	bl	lj_err_caller(PLT)
.L149:
	.align	2
.L148:
	.word	writer_buf-(.LPIC26+8)
	.word	1614
	.size	lj_cf_string_dump, .-lj_cf_string_dump
	.align	2
	.type	lj_ffh_string_reverse, %function
lj_ffh_string_reverse:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, lr}
	mov	r1, #1
	mov	r4, r0
	bl	lj_lib_checkstr(PLT)
	ldr	r1, [r4, #8]
	add	r1, r1, #76
	ldr	r2, [r0, #12]
	mov	r0, r4
	bl	lj_str_needbuf(PLT)
	mov	r0, #0
	ldmfd	sp!, {r4, pc}
	.size	lj_ffh_string_reverse, .-lj_ffh_string_reverse
	.align	2
	.type	lj_ffh_string_char, %function
lj_ffh_string_char:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, r5, r6, r7, r8, lr}
	ldr	r3, [r0, #20]
	ldr	r6, [r0, #16]
	ldr	r1, [r0, #8]
	rsb	r6, r6, r3
	mov	r6, r6, asr #3
	mov	r2, r6
	add	r1, r1, #76
	mov	r7, r0
	bl	lj_str_needbuf(PLT)
	cmp	r6, #0
	mov	r8, r0
	ble	.L156
	sub	r5, r0, #1
	mov	r4, #1
.L155:
	mov	r0, r7
	mov	r1, r4
	bl	lj_lib_checkint(PLT)
	and	r3, r0, #255
	cmp	r3, r0
	bne	.L160
	add	r4, r4, #1
	cmp	r6, r4
	strb	r3, [r5, #1]!
	bge	.L155
.L156:
	mov	r1, r8
	mov	r2, r6
	mov	r0, r7
	ldr	r4, [r7, #16]
	bl	lj_str_new(PLT)
	mvn	r3, #4
	stmdb	r4, {r0, r3}
	mov	r0, #2
	ldmfd	sp!, {r4, r5, r6, r7, r8, pc}
.L160:
	mov	r0, r7
	mov	r1, r4
	ldr	r2, .L161
	bl	lj_err_arg(PLT)
.L162:
	.align	2
.L161:
	.word	537
	.size	lj_ffh_string_char, .-lj_ffh_string_char
	.align	2
	.type	lj_ffh_string_rep, %function
lj_ffh_string_rep:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r3, r4, r5, r6, r7, r8, r9, r10, fp, lr}
	mov	r1, #1
	mov	r7, r0
	bl	lj_lib_checkstr(PLT)
	mov	r1, #2
	mov	fp, r0
	mov	r0, r7
	bl	lj_lib_checkint(PLT)
	mov	r1, #3
	mov	r5, r0
	mov	r0, r7
	bl	lj_lib_optstr(PLT)
	cmp	r5, #0
	ldr	r4, [fp, #12]
	ldr	r10, [r7, #8]
	ble	.L165
	cmp	r0, #0
	mov	r6, r0
	beq	.L167
	ldr	r2, [r0, #12]
	ldr	r0, .L201
	adds	r2, r2, r4
	mov	r3, #0
	adc	r3, r3, r4, asr #31
	mov	r1, #0
	cmp	r0, r2
	sbcs	ip, r1, r3
	blt	.L169
	mul	ip, r5, r3
	mov	r9, r5, asr #31
	mov	r3, ip
	mla	ip, r2, r9, r3
	umull	r8, r9, r5, r2
	add	r9, ip, r9
	cmp	r0, r8
	sbcs	r3, r1, r9
	blt	.L169
	orrs	r3, r8, r9
	bne	.L200
.L165:
	ldr	r4, [r7, #16]
	add	r10, r10, #112
	mvn	r5, #4
	str	r10, [r4, #-8]
	str	r5, [r4, #-4]
	mov	r0, #2
	ldmfd	sp!, {r3, r4, r5, r6, r7, r8, r9, r10, fp, pc}
.L200:
	mov	r2, r8
	mov	r0, r7
	add	r1, r10, #76
	bl	lj_str_needbuf(PLT)
	ldr	r2, [r6, #12]
	add	ip, fp, #16
	subs	r8, r8, r2
	sbc	r9, r9, #0
	cmp	r5, #1
	beq	.L178
	cmp	r4, #0
	ble	.L172
	add	ip, r4, #15
	add	ip, fp, ip
	add	r3, fp, #15
	sub	r2, r0, #1
.L173:
	ldrb	r1, [r3, #1]!	@ zero_extendqisi2
	strb	r1, [r2, #1]!
	cmp	r3, ip
	bne	.L173
	ldr	r2, [r6, #12]
	add	r0, r0, r4
.L172:
	cmp	r2, #0
	ble	.L174
	add	ip, r2, #15
	add	ip, r6, ip
	sub	r3, r0, #1
	add	r6, r6, #15
.L175:
	ldrb	r1, [r6, #1]!	@ zero_extendqisi2
	strb	r1, [r3, #1]!
	cmp	r6, ip
	bne	.L175
	add	r0, r0, r2
.L174:
	ldr	r4, [fp, #12]
	ldr	ip, [r10, #76]
	add	r4, r2, r4
	sub	r5, r5, #1
.L178:
	cmp	r4, #1
	sub	ip, ip, #1
	movge	r1, r4
	movlt	r1, #1
.L177:
	mov	r3, ip
	sub	fp, r0, #1
	mov	r6, #0
.L176:
	add	r6, r6, #1
	ldrb	r2, [r3, #1]!	@ zero_extendqisi2
	cmp	r4, r6
	strb	r2, [fp, #1]!
	bgt	.L176
	subs	r5, r5, #1
	add	r0, r0, r1
	bne	.L177
	ldr	r1, [r10, #76]
	mov	r2, r8
	mov	r0, r7
	ldr	r4, [r7, #16]
	bl	lj_str_new(PLT)
	mvn	r3, #4
	stmdb	r4, {r0, r3}
	mov	r0, #2
	ldmfd	sp!, {r3, r4, r5, r6, r7, r8, r9, r10, fp, pc}
.L167:
	smull	r8, r9, r5, r4
	ldr	r2, .L201
	mov	r3, #0
	cmp	r2, r8
	sbcs	r3, r3, r9
	blt	.L169
	orrs	r3, r8, r9
	beq	.L165
	mov	r0, r7
	add	r1, r10, #76
	mov	r2, r8
	bl	lj_str_needbuf(PLT)
	add	ip, fp, #16
	b	.L178
.L169:
	mov	r0, r7
	mov	r1, #56
	bl	lj_err_caller(PLT)
.L202:
	.align	2
.L201:
	.word	2147483392
	.size	lj_ffh_string_rep, .-lj_ffh_string_rep
	.align	2
	.type	lj_ffh_string_sub, %function
lj_ffh_string_sub:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, lr}
	mov	r1, #1
	mov	r4, r0
	bl	lj_lib_checkstr(PLT)
	mov	r0, r4
	mov	r1, #2
	bl	lj_lib_checkint(PLT)
	mov	r0, r4
	mov	r1, #3
	mvn	r2, #0
	ldr	r4, [r4, #16]
	bl	lj_lib_optint(PLT)
	mvn	r3, #13
	str	r3, [r4, #20]
	str	r0, [r4, #16]
	mov	r0, #0
	ldmfd	sp!, {r4, pc}
	.size	lj_ffh_string_sub, .-lj_ffh_string_sub
	.align	2
	.type	lj_ffh_string_byte, %function
lj_ffh_string_byte:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, r5, r6, r7, r8, lr}
	mov	r1, #1
	mov	r6, r0
	bl	lj_lib_checkstr(PLT)
	mov	r1, #2
	mov	r2, #1
	mov	r4, r0
	mov	r0, r6
	ldr	r8, [r4, #12]
	bl	lj_lib_optint(PLT)
	mov	r1, #3
	mov	r7, r0
	mov	r2, r7
	mov	r0, r6
	bl	lj_lib_optint(PLT)
	cmp	r0, #0
	addlt	r3, r8, #1
	addlt	r0, r0, r3
	cmp	r7, #0
	addlt	r3, r8, #1
	addlt	r7, r7, r3
	cmp	r7, #1
	movlt	r7, #1
	cmp	r0, r8
	movlt	r5, r0
	movge	r5, r8
	cmp	r7, r5
	bgt	.L215
	sub	r0, r7, #1
	rsb	r5, r0, r5
	cmp	r5, #8000
	bhi	.L218
	ldr	r3, [r6, #24]
	ldr	r2, [r6, #20]
	rsb	r3, r2, r3
	cmp	r3, r5, asl #3
	ble	.L219
.L211:
	cmp	r5, #0
	beq	.L214
	add	r2, r7, #14
	add	r0, r5, r2
	add	r0, r4, r0
	add	r2, r4, r2
	mvn	r3, #7
	mvn	r4, #13
.L213:
	ldr	r1, [r6, #16]
	ldrb	lr, [r2, #1]!	@ zero_extendqisi2
	add	ip, r1, r3
	cmp	r2, r0
	str	lr, [r1, r3]
	str	r4, [ip, #4]
	add	r3, r3, #8
	bne	.L213
.L214:
	add	r0, r5, #1
	ldmfd	sp!, {r4, r5, r6, r7, r8, pc}
.L215:
	mov	r0, #1
	ldmfd	sp!, {r4, r5, r6, r7, r8, pc}
.L219:
	mov	r0, r6
	mov	r1, r5
	bl	lj_state_growstack(PLT)
	b	.L211
.L218:
	mov	r0, r6
	ldr	r1, .L220
	bl	lj_err_caller(PLT)
.L221:
	.align	2
.L220:
	.word	1644
	.size	lj_ffh_string_byte, .-lj_ffh_string_byte
	.align	2
	.type	classend.isra.6, %function
classend.isra.6:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r3, lr}
	ldrb	r3, [r1]	@ zero_extendqisi2
	add	r2, r1, #1
	cmp	r3, #37
	beq	.L224
	cmp	r3, #91
	bne	.L240
	ldrb	r3, [r1, #1]	@ zero_extendqisi2
	cmp	r3, #94
	addeq	r2, r1, #2
	ldreqb	r3, [r1, #2]	@ zero_extendqisi2
	b	.L228
.L231:
	cmp	r3, #93
	beq	.L241
.L228:
	cmp	r3, #0
	beq	.L236
	cmp	r3, #37
	add	r1, r2, #1
	ldrb	r3, [r2, #1]	@ zero_extendqisi2
	movne	r2, r1
	bne	.L231
	cmp	r3, #0
	bne	.L242
.L236:
	ldr	r0, [r0]
	ldr	r1, .L244
	bl	lj_err_caller(PLT)
.L240:
	mov	r0, r2
	ldmfd	sp!, {r3, pc}
.L242:
	ldrb	r3, [r2, #2]	@ zero_extendqisi2
	add	r2, r2, #2
	cmp	r3, #93
	bne	.L228
.L241:
	add	r0, r2, #1
	ldmfd	sp!, {r3, pc}
.L224:
	ldrb	r3, [r1, #1]	@ zero_extendqisi2
	cmp	r3, #0
	beq	.L243
	add	r0, r1, #2
	ldmfd	sp!, {r3, pc}
.L243:
	ldr	r0, [r0]
	ldr	r1, .L244+4
	bl	lj_err_caller(PLT)
.L245:
	.align	2
.L244:
	.word	1758
	.word	1724
	.size	classend.isra.6, .-classend.isra.6
	.align	2
	.type	push_onecapture, %function
push_onecapture:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	ldr	ip, [r0, #12]
	stmfd	sp!, {r3, lr}
	cmp	ip, r1
	bgt	.L247
	cmp	r1, #0
	bne	.L248
	mov	ip, r2
	ldr	r0, [r0, #8]
	rsb	r2, r2, r3
	mov	r1, ip
	ldmfd	sp!, {r3, lr}
	b	lua_pushlstring(PLT)
.L247:
	add	r1, r0, r1, asl #3
	ldr	r2, [r1, #24]
	cmn	r2, #1
	beq	.L252
	cmn	r2, #2
	beq	.L253
	ldr	r0, [r0, #8]
	ldr	r1, [r1, #20]
	ldmfd	sp!, {r3, lr}
	b	lua_pushlstring(PLT)
.L253:
	ldr	r3, [r1, #20]
	ldr	r1, [r0]
	ldr	r0, [r0, #8]
	rsb	r1, r1, r3
	add	r1, r1, #1
	ldmfd	sp!, {r3, lr}
	b	lua_pushinteger(PLT)
.L252:
	ldr	r0, [r0, #8]
	ldr	r1, .L254
	bl	lj_err_caller(PLT)
.L248:
	ldr	r0, [r0, #8]
	ldr	r1, .L254+4
	bl	lj_err_caller(PLT)
.L255:
	.align	2
.L254:
	.word	1869
	.word	1829
	.size	push_onecapture, .-push_onecapture
	.align	2
	.type	push_captures, %function
push_captures:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, r5, r6, r7, r8, lr}
	ldr	r5, [r0, #12]
	mov	r6, r0
	adds	r3, r5, #0
	movne	r3, #1
	cmp	r1, #0
	orreq	r3, r3, #1
	cmp	r3, #0
	mov	r8, r2
	ldr	r0, [r0, #8]
	mov	r7, r1
	bne	.L264
	ldr	r2, .L265
	mov	r1, #1
.LPIC28:
	add	r2, pc, r2
	bl	luaL_checkstack(PLT)
	mov	r5, #1
.L260:
	mov	r4, #0
.L259:
	mov	r1, r4
	mov	r0, r6
	mov	r2, r7
	add	r4, r4, #1
	mov	r3, r8
	bl	push_onecapture(PLT)
	cmp	r4, r5
	blt	.L259
.L261:
	mov	r0, r5
	ldmfd	sp!, {r4, r5, r6, r7, r8, pc}
.L264:
	ldr	r2, .L265+4
	mov	r1, r5
.LPIC27:
	add	r2, pc, r2
	bl	luaL_checkstack(PLT)
	cmp	r5, #0
	bgt	.L260
	b	.L261
.L266:
	.align	2
.L265:
	.word	.LC8-(.LPIC28+8)
	.word	.LC8-(.LPIC27+8)
	.size	push_captures, .-push_captures
	.align	2
	.type	matchbracketclass, %function
matchbracketclass:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, r5, r6, lr}
	ldr	r4, .L290
	ldrb	r3, [r1, #1]	@ zero_extendqisi2
.LPIC30:
	add	r4, pc, r4
	ldr	r6, .L290+4
	cmp	r3, #94
	add	r4, r4, #1
	addeq	r1, r1, #1
	moveq	r5, #0
	movne	r5, #1
.LPIC29:
	add	r6, pc, r6
	add	r4, r0, r4
	b	.L269
.L270:
	ldrb	lr, [r1, #2]	@ zero_extendqisi2
	cmp	lr, #45
	beq	.L286
.L277:
	cmp	ip, r0
	beq	.L284
.L276:
	mov	r1, r3
.L269:
	add	r3, r1, #1
	cmp	r3, r2
	bcs	.L287
	ldrb	ip, [r1, #1]	@ zero_extendqisi2
	cmp	ip, #37
	bne	.L270
	ldrb	ip, [r1, #2]	@ zero_extendqisi2
	add	r3, r1, #2
	and	r1, ip, #192
	cmp	r1, #64
	beq	.L288
.L271:
	rsb	ip, ip, r0
	clz	ip, ip
	mov	ip, ip, lsr #5
.L273:
	cmp	ip, #0
	beq	.L276
.L284:
	mov	r0, r5
	ldmfd	sp!, {r4, r5, r6, pc}
.L286:
	add	lr, r1, #3
	cmp	r2, lr
	bls	.L277
	cmp	ip, r0
	bgt	.L282
	ldrb	r3, [r1, #3]	@ zero_extendqisi2
	cmp	r0, r3
	ble	.L284
.L282:
	mov	r3, lr
	b	.L276
.L288:
	and	r1, ip, #31
	ldrb	r1, [r6, r1]	@ zero_extendqisi2
	cmp	r1, #0
	beq	.L272
	ldrb	lr, [r4]	@ zero_extendqisi2
	tst	ip, #32
	and	ip, r1, lr
	clzeq	ip, ip
	moveq	ip, ip, lsr #5
	b	.L273
.L272:
	cmp	ip, #122
	beq	.L289
	cmp	ip, #90
	bne	.L271
	adds	ip, r0, #0
	movne	ip, #1
	b	.L273
.L287:
	eor	r0, r5, #1
	ldmfd	sp!, {r4, r5, r6, pc}
.L289:
	clz	ip, r0
	mov	ip, ip, lsr #5
	b	.L273
.L291:
	.align	2
.L290:
	.word	lj_char_bits-(.LPIC30+8)
	.word	.LANCHOR0-(.LPIC29+8)
	.size	matchbracketclass, .-matchbracketclass
	.align	2
	.type	match, %function
match:
	@ args = 0, pretend = 0, frame = 8
	@ frame_needed = 0, uses_anonymous_args = 0
	ldr	r3, [r0, #16]
	stmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, lr}
	add	r3, r3, #1
	cmp	r3, #200
	sub	sp, sp, #12
	mov	r5, r0
	str	r3, [r0, #16]
	bgt	.L293
	ldr	r9, .L415
	ldr	r8, .L415+4
	ldr	fp, .L415+8
.LPIC41:
	add	r9, pc, r9
.LPIC39:
	add	r8, pc, r8
	ldrb	r4, [r2]	@ zero_extendqisi2
	mov	r7, r1
	mov	r6, r2
	add	r9, r9, #1
	add	r8, r8, #1
.LPIC40:
	add	fp, pc, fp
	add	r10, r0, #8
.L294:
	cmp	r4, #41
	addls	pc, pc, r4, asl #2
	b	.L295
.L297:
	b	.L296
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L295
	b	.L298
	b	.L299
	b	.L295
	b	.L295
	b	.L300
	b	.L301
	.p2align 1
.L343:
	add	r1, r7, #1
	mov	r2, r6
	mov	r0, r5
	bl	max_expand(PLT)
.L398:
	mov	r7, r0
.L296:
	ldr	r3, [r5, #16]
	mov	r0, r7
	sub	r3, r3, #1
	str	r3, [r5, #16]
	add	sp, sp, #12
	@ sp needed
	ldmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, pc}
.L295:
	mov	r0, r10
	mov	r1, r6
	bl	classend.isra.6(PLT)
	ldr	r2, [r5, #4]
	cmp	r7, r2
	mov	r3, r0
	bcc	.L404
.L330:
	ldrb	r2, [r3]	@ zero_extendqisi2
	sub	r2, r2, #42
	cmp	r2, #21
	addls	pc, pc, r2, asl #2
	b	.L366
.L356:
	b	.L341
	b	.L366
	b	.L366
	b	.L344
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L366
	b	.L357
	.p2align 1
.L301:
	ldr	r3, [r5, #12]
	add	r2, r6, #1
	subs	r1, r3, #1
	bmi	.L305
	add	r0, r5, r3, asl #3
	ldr	r0, [r0, #16]
	cmn	r0, #1
	addne	r3, r5, r3, asl #3
	beq	.L306
.L308:
	subs	r1, r1, #1
	bcc	.L305
	sub	r3, r3, #8
	ldr	r0, [r3, #16]
	cmn	r0, #1
	bne	.L308
.L306:
	add	r4, r5, r1, asl #3
	ldr	r3, [r4, #20]
	mov	r1, r7
	rsb	r3, r3, r7
	str	r3, [r4, #24]
	mov	r0, r5
	bl	match(PLT)
	subs	r7, r0, #0
	mvneq	r3, #0
	streq	r3, [r4, #24]
	b	.L296
.L300:
	ldrb	r3, [r6, #1]	@ zero_extendqisi2
	cmp	r3, #41
	ldr	r3, [r5, #12]
	beq	.L405
	cmp	r3, #31
	add	r2, r6, #1
	bgt	.L304
	add	r0, r5, r3, asl #3
	mvn	r4, #0
	add	r3, r3, #1
	str	r7, [r0, #20]
	str	r4, [r0, #24]
	mov	r1, r7
	mov	r0, r5
	str	r3, [r5, #12]
	bl	match(PLT)
	subs	r7, r0, #0
	ldreq	r3, [r5, #12]
	addeq	r3, r3, r4
	streq	r3, [r5, #12]
	b	.L296
.L298:
	ldrb	r3, [r6, #1]	@ zero_extendqisi2
	cmp	r3, #0
	beq	.L406
	mov	r0, r10
	mov	r1, r6
	bl	classend.isra.6(PLT)
	ldr	r2, [r5, #4]
	cmp	r7, r2
	mov	r3, r0
	bcs	.L330
	ldrb	r2, [r7]	@ zero_extendqisi2
.L331:
	rsb	r0, r4, r2
	clz	r0, r0
	mov	r0, r0, lsr #5
.L337:
	cmp	r0, #0
	beq	.L330
.L339:
	ldrb	r4, [r3]	@ zero_extendqisi2
	sub	r2, r4, #42
	cmp	r2, #21
	addls	pc, pc, r2, asl #2
	b	.L340
.L355:
	b	.L341
	b	.L343
	b	.L340
	b	.L344
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L340
	b	.L345
	.p2align 1
.L299:
	ldrb	r4, [r6, #1]	@ zero_extendqisi2
	cmp	r4, #98
	beq	.L312
	cmp	r4, #102
	bne	.L407
	ldrb	r3, [r6, #2]	@ zero_extendqisi2
	add	r4, r6, #2
	cmp	r3, #91
	bne	.L408
	mov	r1, r4
	mov	r0, r10
	bl	classend.isra.6(PLT)
	ldr	r3, [r5]
	mov	r1, r4
	cmp	r3, r7
	mov	r6, r0
	sub	r3, r6, #1
	ldrneb	r0, [r7, #-1]	@ zero_extendqisi2
	moveq	r0, #0
	mov	r2, r3
	str	r3, [sp, #4]
	bl	matchbracketclass(PLT)
	cmp	r0, #0
	bne	.L366
	ldr	r3, [sp, #4]
	mov	r1, r4
	mov	r2, r3
	ldrb	r0, [r7]	@ zero_extendqisi2
	bl	matchbracketclass(PLT)
	cmp	r0, #0
	beq	.L366
	ldrb	r4, [r6]	@ zero_extendqisi2
	b	.L294
.L406:
	ldr	r3, [r5, #4]
	cmp	r3, r7
	beq	.L296
.L366:
	mov	r7, #0
	b	.L296
.L404:
	ldrb	r0, [r7]	@ zero_extendqisi2
	cmp	r4, #46
	mov	r2, r0
	beq	.L339
	cmp	r4, #91
	beq	.L333
	cmp	r4, #37
	bne	.L331
	ldrb	r4, [r6, #1]	@ zero_extendqisi2
.L334:
	and	r2, r4, #192
	cmp	r2, #64
	beq	.L409
.L335:
	rsb	r0, r4, r0
	clz	r0, r0
	mov	r0, r0, lsr #5
	b	.L337
.L344:
	ldr	r9, .L415+12
	ldr	fp, .L415+16
.LPIC43:
	add	r9, pc, r9
	mov	r4, r7
	add	r9, r9, #1
	add	r8, r3, #1
	sub	r10, r3, #1
.LPIC42:
	add	fp, pc, fp
.L346:
	mov	r0, r5
	mov	r1, r4
	mov	r2, r8
	bl	match(PLT)
	subs	r7, r0, #0
	bne	.L296
	ldr	r3, [r5, #4]
	cmp	r4, r3
	bcs	.L366
	ldrb	r3, [r6]	@ zero_extendqisi2
	ldrb	r0, [r4], #1	@ zero_extendqisi2
	cmp	r3, #46
	beq	.L346
	cmp	r3, #91
	beq	.L349
	cmp	r3, #37
	beq	.L410
	rsb	r0, r3, r0
	clz	r0, r0
	mov	r0, r0, lsr #5
.L353:
	cmp	r0, #0
	bne	.L346
	b	.L366
.L341:
	mov	r1, r7
	mov	r2, r6
	mov	r0, r5
	bl	max_expand(PLT)
	mov	r7, r0
	b	.L296
.L357:
	add	r6, r3, #1
.L360:
	ldrb	r4, [r3, #1]	@ zero_extendqisi2
	b	.L294
.L312:
	ldrb	ip, [r6, #2]	@ zero_extendqisi2
	cmp	ip, #0
	beq	.L314
	ldrb	r4, [r6, #3]	@ zero_extendqisi2
	cmp	r4, #0
	beq	.L314
	ldrb	r3, [r7]	@ zero_extendqisi2
	cmp	r3, ip
	bne	.L366
	ldr	r2, [r5, #4]
	add	r3, r7, #1
	mov	r0, #1
.L317:
	cmp	r2, r3
	mov	r7, r3
	bls	.L366
	ldrb	r1, [r3], #1	@ zero_extendqisi2
	cmp	r4, r1
	beq	.L411
.L318:
	cmp	ip, r1
	addeq	r0, r0, #1
	cmp	r2, r3
	mov	r7, r3
	bls	.L366
	ldrb	r1, [r3], #1	@ zero_extendqisi2
	cmp	r4, r1
	bne	.L318
.L411:
	subs	r0, r0, #1
	bne	.L317
	adds	r7, r7, #1
	beq	.L366
	ldrb	r4, [r6, #4]	@ zero_extendqisi2
	add	r6, r6, #4
	b	.L294
.L405:
	cmp	r3, #31
	add	r2, r6, #2
	bgt	.L304
	add	r0, r5, r3, asl #3
	mvn	ip, #1
	add	r3, r3, #1
	str	r7, [r0, #20]
	str	ip, [r0, #24]
	mov	r1, r7
	mov	r0, r5
	str	r3, [r5, #12]
	bl	match(PLT)
	subs	r7, r0, #0
	ldreq	r3, [r5, #12]
	subeq	r3, r3, #1
	streq	r3, [r5, #12]
	b	.L296
.L345:
	add	r6, r3, #1
	mov	r2, r6
	mov	r0, r5
	add	r1, r7, #1
	str	r3, [sp, #4]
	bl	match(PLT)
	cmp	r0, #0
	bne	.L398
	ldr	r3, [sp, #4]
	b	.L360
.L340:
	add	r7, r7, #1
	mov	r6, r3
	b	.L294
.L410:
	ldrb	r2, [r6, #1]	@ zero_extendqisi2
	and	r3, r2, #192
	cmp	r3, #64
	beq	.L412
.L351:
	rsb	r0, r2, r0
	clz	r0, r0
	mov	r0, r0, lsr #5
	b	.L353
.L349:
	mov	r1, r6
	mov	r2, r10
	bl	matchbracketclass(PLT)
	b	.L353
.L412:
	and	r3, r2, #31
	ldrb	r1, [fp, r3]	@ zero_extendqisi2
	cmp	r1, #0
	bne	.L413
	cmp	r2, #122
	beq	.L397
	cmp	r2, #90
	bne	.L351
	adds	r0, r0, #0
	movne	r0, #1
	b	.L353
.L407:
	ldrb	r3, [r8, r4]	@ zero_extendqisi2
	tst	r3, #8
	bne	.L414
	mov	r0, r10
	mov	r1, r6
	bl	classend.isra.6(PLT)
	ldr	r2, [r5, #4]
	cmp	r7, r2
	mov	r3, r0
	bcs	.L330
	ldrb	r0, [r7]	@ zero_extendqisi2
	b	.L334
.L413:
	ldrb	r3, [r0, r9]	@ zero_extendqisi2
	tst	r2, #32
	and	r0, r1, r3
	bne	.L353
.L397:
	clz	r0, r0
	mov	r0, r0, lsr #5
	b	.L353
.L333:
	sub	r2, r3, #1
	mov	r1, r6
	str	r3, [sp, #4]
	bl	matchbracketclass(PLT)
	ldr	r3, [sp, #4]
	b	.L337
.L414:
	subs	r2, r4, #49
	bmi	.L327
	ldr	r3, [r5, #12]
	cmp	r2, r3
	bge	.L327
	add	r2, r5, r4, asl #3
	ldr	r4, [r2, #-368]
	cmn	r4, #1
	beq	.L327
	ldr	r3, [r5, #4]
	rsb	r3, r7, r3
	cmp	r4, r3
	bhi	.L366
	ldr	r0, [r2, #-372]
	mov	r1, r7
	mov	r2, r4
	bl	memcmp(PLT)
	cmp	r0, #0
	bne	.L366
	adds	r7, r7, r4
	beq	.L366
	ldrb	r4, [r6, #2]	@ zero_extendqisi2
	add	r6, r6, #2
	b	.L294
.L409:
	and	r2, r4, #31
	ldrb	r1, [fp, r2]	@ zero_extendqisi2
	cmp	r1, #0
	beq	.L336
	ldrb	r2, [r0, r9]	@ zero_extendqisi2
	tst	r4, #32
	and	r0, r1, r2
	bne	.L337
.L396:
	clz	r0, r0
	mov	r0, r0, lsr #5
	b	.L337
.L336:
	cmp	r4, #122
	beq	.L396
	cmp	r4, #90
	bne	.L335
	adds	r0, r0, #0
	movne	r0, #1
	b	.L337
.L305:
	ldr	r0, [r5, #8]
	ldr	r1, .L415+20
	bl	lj_err_caller(PLT)
.L293:
	ldr	r0, [r0, #8]
	ldr	r1, .L415+24
	bl	lj_err_caller(PLT)
.L304:
	ldr	r0, [r5, #8]
	ldr	r1, .L415+28
	bl	lj_err_caller(PLT)
.L327:
	ldr	r0, [r5, #8]
	ldr	r1, .L415+32
	bl	lj_err_caller(PLT)
.L314:
	ldr	r0, [r5, #8]
	ldr	r1, .L415+36
	bl	lj_err_caller(PLT)
.L408:
	ldr	r0, [r5, #8]
	ldr	r1, .L415+40
	bl	lj_err_caller(PLT)
.L416:
	.align	2
.L415:
	.word	lj_char_bits-(.LPIC41+8)
	.word	lj_char_bits-(.LPIC39+8)
	.word	.LANCHOR0-(.LPIC40+8)
	.word	lj_char_bits-(.LPIC43+8)
	.word	.LANCHOR0-(.LPIC42+8)
	.word	1700
	.word	1809
	.word	1851
	.word	1829
	.word	1790
	.word	1666
	.size	match, .-match
	.align	2
	.type	max_expand, %function
max_expand:
	@ args = 0, pretend = 0, frame = 16
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, lr}
	mov	r8, r0
	ldr	r0, [r0, #4]
	sub	sp, sp, #20
	cmp	r0, r1
	mov	r10, r1
	str	r3, [sp, #4]
	movls	r4, #0
	subls	r9, r1, #1
	bls	.L419
	mov	fp, r2
	ldr	r3, .L445
	ldr	r2, .L445+4
.LPIC44:
	add	r3, pc, r3
.LPIC45:
	add	r2, pc, r2
	str	r3, [sp, #8]
	add	r3, r2, #1
	str	r3, [sp, #12]
	ldr	r3, [sp, #4]
	sub	r7, r0, #1
	sub	r3, r3, #1
	sub	r9, r1, #1
	str	r3, [sp]
	mov	r3, fp
	ldrb	r6, [fp]	@ zero_extendqisi2
	mov	r5, r9
	mov	fp, r7
	mov	r4, #0
	mov	r7, r3
	b	.L421
.L443:
	cmp	r6, #37
	beq	.L442
	rsb	r0, r6, r0
	clz	r0, r0
	mov	r0, r0, lsr #5
.L428:
	cmp	r0, #0
	beq	.L419
.L420:
	cmp	r5, fp
	add	r4, r4, #1
	beq	.L419
.L421:
	cmp	r6, #46
	ldrb	r0, [r5, #1]!	@ zero_extendqisi2
	beq	.L420
	cmp	r6, #91
	bne	.L443
	mov	r1, r7
	ldr	r2, [sp]
	bl	matchbracketclass(PLT)
	cmp	r0, #0
	bne	.L420
.L419:
	ldr	r3, [sp, #4]
	add	r4, r10, r4
	add	r7, r3, #1
.L422:
	mov	r1, r4
	mov	r0, r8
	mov	r2, r7
	bl	match(PLT)
	sub	r4, r4, #1
	cmp	r0, #0
	bne	.L430
	cmp	r4, r9
	bne	.L422
.L430:
	add	sp, sp, #20
	@ sp needed
	ldmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, pc}
.L442:
	ldrb	r2, [r7, #1]	@ zero_extendqisi2
	and	r1, r2, #192
	cmp	r1, #64
	beq	.L444
.L426:
	rsb	r0, r2, r0
	clz	r0, r0
	mov	r0, r0, lsr #5
	b	.L428
.L444:
	ldr	r3, [sp, #8]
	and	r1, r2, #31
	ldrb	r1, [r3, r1]	@ zero_extendqisi2
	cmp	r1, #0
	beq	.L427
	ldr	r3, [sp, #12]
	tst	r2, #32
	ldrb	r0, [r0, r3]	@ zero_extendqisi2
	and	r0, r0, r1
	bne	.L428
.L440:
	clz	r0, r0
	mov	r0, r0, lsr #5
	b	.L428
.L427:
	cmp	r2, #122
	beq	.L440
	cmp	r2, #90
	bne	.L426
	adds	r0, r0, #0
	movne	r0, #1
	b	.L428
.L446:
	.align	2
.L445:
	.word	.LANCHOR0-(.LPIC44+8)
	.word	lj_char_bits-(.LPIC45+8)
	.size	max_expand, .-max_expand
	.align	2
	.type	lj_cf_string_gsub, %function
lj_cf_string_gsub:
	@ args = 0, pretend = 0, frame = 1360
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, lr}
	sub	sp, sp, #1360
	sub	sp, sp, #4
	add	fp, sp, #48
	sub	r2, fp, #8
	mov	r1, #1
	mov	r6, r0
	str	r0, [sp, #28]
	bl	luaL_checklstring(PLT)
	mov	r2, #0
	mov	r1, #2
	mov	r5, r0
	mov	r0, r6
	bl	luaL_checklstring(PLT)
	mov	r1, #3
	mov	r7, r0
	str	r0, [sp, #4]
	mov	r0, r6
	bl	lua_type(PLT)
	ldr	r2, [sp, #40]
	mov	r1, #4
	add	r2, r2, #1
	mov	r4, r0
	mov	r0, r6
	bl	luaL_optinteger(PLT)
	ldrb	r3, [r7]	@ zero_extendqisi2
	sub	r4, r4, #3
	cmp	r3, #94
	ldreq	r3, [sp, #4]
	movne	r3, #0
	addeq	r3, r3, #1
	streq	r3, [sp, #4]
	moveq	r3, #1
	cmp	r4, #3
	str	r3, [sp, #12]
	str	r0, [sp, #8]
	bhi	.L484
	ldr	r4, [sp, #28]
	add	r10, sp, #324
	mov	r1, r10
	mov	r0, r4
	bl	luaL_buffinit(PLT)
	ldr	r2, [sp, #40]
	ldr	r3, [sp, #8]
	add	r2, r5, r2
	cmp	r3, #0
	str	r4, [sp, #56]
	str	r5, [sp, #48]
	str	r2, [sp, #52]
	ble	.L473
	ldr	r3, .L491
	mov	r7, #0
.LPIC46:
	add	r3, pc, r3
	add	r3, r3, #1
	mov	r9, r7
	add	r8, sp, #1360
	str	r3, [sp, #16]
.L471:
	mov	r0, fp
	mov	r1, r5
	ldr	r2, [sp, #4]
	str	r9, [sp, #64]
	str	r9, [sp, #60]
	bl	match(PLT)
	subs	r6, r0, #0
	beq	.L451
	ldr	r4, [sp, #56]
	mov	r1, #3
	mov	r0, r4
	bl	lua_type(PLT)
	add	r7, r7, #1
	sub	r0, r0, #3
	cmp	r0, #3
	addls	pc, pc, r0, asl #2
	b	.L452
.L454:
	b	.L453
	b	.L453
	b	.L455
	b	.L456
	.p2align 1
.L456:
	mov	r0, r4
	mov	r1, #3
	bl	lua_pushvalue(PLT)
	mov	r1, r5
	mov	r2, r6
	mov	r0, fp
	bl	push_captures(PLT)
	mov	r2, #1
	mov	r1, r0
	mov	r0, r4
	bl	lua_call(PLT)
.L452:
	mov	r0, r4
	mvn	r1, #0
	bl	lua_toboolean(PLT)
	cmp	r0, #0
	mov	r0, r4
	bne	.L466
	mvn	r1, #1
	bl	lua_settop(PLT)
	mov	r0, r4
	mov	r1, r5
	rsb	r2, r5, r6
	bl	lua_pushlstring(PLT)
.L467:
	mov	r0, r10
	bl	luaL_addvalue(PLT)
.L458:
	cmp	r6, r5
	movhi	r5, r6
	bls	.L451
.L468:
	ldr	r3, [sp, #12]
	cmp	r3, #0
	bne	.L483
	ldr	r3, [sp, #8]
	cmp	r3, r7
	bgt	.L471
.L483:
	ldr	r2, [sp, #52]
.L450:
	rsb	r2, r5, r2
	mov	r1, r5
	mov	r0, r10
	bl	luaL_addlstring(PLT)
	mov	r0, r10
	bl	luaL_pushresult(PLT)
	ldr	r0, [sp, #28]
	mov	r1, r7
	bl	lua_pushinteger(PLT)
	mov	r0, #2
	add	sp, sp, #1360
	add	sp, sp, #4
	@ sp needed
	ldmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, pc}
.L455:
	mov	r0, fp
	mov	r1, #0
	mov	r2, r5
	mov	r3, r6
	bl	push_onecapture(PLT)
	mov	r0, r4
	mov	r1, #3
	bl	lua_gettable(PLT)
	b	.L452
.L453:
	ldr	r0, [sp, #56]
	mov	r1, #3
	sub	r2, fp, #4
	bl	lua_tolstring(PLT)
	ldr	r3, [sp, #44]
	cmp	r3, #0
	beq	.L458
	rsb	r3, r5, r6
	str	r6, [sp, #20]
	mov	r4, #0
	mov	r6, r5
	str	r3, [sp, #32]
	mov	r5, r0
	str	r7, [sp, #24]
	b	.L457
.L487:
	ldr	r3, [sp, #324]
	cmp	r3, r8
	bcs	.L485
.L460:
	add	r1, r3, #1
	str	r1, [sp, #324]
	ldrb	r2, [r7]	@ zero_extendqisi2
	strb	r2, [r3]
.L461:
	ldr	r3, [sp, #44]
	add	r4, r4, #1
	cmp	r4, r3
	bcs	.L486
.L457:
	ldrb	r3, [r5, r4]	@ zero_extendqisi2
	add	r7, r5, r4
	cmp	r3, #37
	bne	.L487
	add	r4, r4, #1
	ldrb	r1, [r5, r4]	@ zero_extendqisi2
	ldr	r3, [sp, #16]
	add	r2, r5, r4
	ldrb	r3, [r1, r3]	@ zero_extendqisi2
	tst	r3, #8
	bne	.L462
	ldr	r3, [sp, #324]
	cmp	r3, r8
	bcs	.L488
.L463:
	add	r1, r3, #1
	str	r1, [sp, #324]
	ldrb	r2, [r2]	@ zero_extendqisi2
	strb	r2, [r3]
	ldr	r3, [sp, #44]
	add	r4, r4, #1
	cmp	r4, r3
	bcc	.L457
.L486:
	mov	r5, r6
	ldr	r6, [sp, #20]
	ldr	r7, [sp, #24]
	cmp	r6, r5
	movhi	r5, r6
	bhi	.L468
.L451:
	ldr	r2, [sp, #52]
	cmp	r2, r5
	bls	.L450
	ldr	r3, [sp, #324]
	cmp	r3, r8
	bcs	.L489
.L469:
	add	r2, r3, #1
	str	r2, [sp, #324]
	ldrb	r2, [r5]	@ zero_extendqisi2
	strb	r2, [r3]
	add	r5, r5, #1
	b	.L468
.L485:
	mov	r0, r10
	bl	luaL_prepbuffer(PLT)
	ldr	r3, [sp, #324]
	b	.L460
.L466:
	mvn	r1, #0
	bl	lua_isstring(PLT)
	cmp	r0, #0
	bne	.L467
	mvn	r1, #0
	mov	r0, r4
	bl	lua_type(PLT)
	mov	r1, r0
	mov	r0, r4
	bl	lua_typename(PLT)
	ldr	r1, .L491+4
	mov	r2, r0
	mov	r0, r4
	bl	lj_err_callerv(PLT)
.L462:
	cmp	r1, #48
	beq	.L490
	mov	r0, fp
	sub	r1, r1, #49
	mov	r2, r6
	ldr	r3, [sp, #20]
	bl	push_onecapture(PLT)
	mov	r0, r10
	bl	luaL_addvalue(PLT)
	b	.L461
.L489:
	mov	r0, r10
	bl	luaL_prepbuffer(PLT)
	ldr	r3, [sp, #324]
	b	.L469
.L488:
	mov	r0, r10
	str	r2, [sp, #36]
	bl	luaL_prepbuffer(PLT)
	ldr	r3, [sp, #324]
	ldr	r2, [sp, #36]
	b	.L463
.L490:
	mov	r0, r10
	mov	r1, r6
	ldr	r2, [sp, #32]
	bl	luaL_addlstring(PLT)
	b	.L461
.L473:
	mov	r7, #0
	b	.L450
.L484:
	ldr	r0, [sp, #28]
	mov	r1, #3
	mov	r2, #656
	bl	lj_err_arg(PLT)
.L492:
	.align	2
.L491:
	.word	lj_char_bits-(.LPIC46+8)
	.word	1999
	.size	lj_cf_string_gsub, .-lj_cf_string_gsub
	.align	2
	.type	lj_cf_string_gmatch_aux, %function
lj_cf_string_gmatch_aux:
	@ args = 0, pretend = 0, frame = 280
	@ frame_needed = 0, uses_anonymous_args = 0
	ldr	r3, [r0, #16]
	stmfd	sp!, {r4, r5, r6, r7, r8, r9, lr}
	ldr	r8, [r3, #-8]
	sub	sp, sp, #284
	ldr	r7, [r8, #24]
	ldr	r4, [r8, #40]
	ldr	r3, [r7, #12]
	add	r7, r7, #16
	add	r3, r7, r3
	add	r4, r7, r4
	ldr	r6, [r8, #32]
	cmp	r4, r3
	str	r0, [sp, #12]
	str	r3, [sp, #8]
	str	r7, [sp, #4]
	add	r6, r6, #16
	addls	r9, sp, #4
	movls	r5, #0
	bls	.L498
	b	.L499
.L496:
	ldr	r3, [sp, #8]
	add	r4, r4, #1
	cmp	r3, r4
	bcc	.L499
.L498:
	mov	r2, r6
	mov	r0, r9
	mov	r1, r4
	str	r5, [sp, #20]
	str	r5, [sp, #16]
	bl	match(PLT)
	subs	r2, r0, #0
	beq	.L496
	rsb	r7, r7, r2
	cmp	r2, r4
	addeq	r7, r7, #1
	str	r7, [r8, #40]
	mov	r0, r9
	mov	r1, r4
	bl	push_captures(PLT)
	add	sp, sp, #284
	@ sp needed
	ldmfd	sp!, {r4, r5, r6, r7, r8, r9, pc}
.L499:
	mov	r0, #0
	add	sp, sp, #284
	@ sp needed
	ldmfd	sp!, {r4, r5, r6, r7, r8, r9, pc}
	.size	lj_cf_string_gmatch_aux, .-lj_cf_string_gmatch_aux
	.align	2
	.type	str_find_aux, %function
str_find_aux:
	@ args = 0, pretend = 0, frame = 296
	@ frame_needed = 0, uses_anonymous_args = 0
	stmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, lr}
	sub	sp, sp, #300
	add	r2, sp, #12
	mov	r8, r1
	mov	r1, #1
	mov	r9, r0
	bl	luaL_checklstring(PLT)
	mov	r1, #2
	add	r2, sp, #16
	str	r0, [sp]
	mov	r0, r9
	bl	luaL_checklstring(PLT)
	mov	r2, #1
	mov	r1, #3
	mov	r7, r0
	mov	r0, r9
	bl	luaL_optinteger(PLT)
	ldr	r3, [sp, #12]
	cmp	r0, #0
	addlt	r2, r3, #1
	addlt	r0, r0, r2
	bic	r0, r0, r0, asr #31
	subs	r5, r0, #1
	movcc	r4, #0
	bcc	.L504
	cmp	r3, r5
	movcc	r4, r3
	movcs	r4, r5
.L504:
	cmp	r8, #0
	beq	.L505
	mov	r0, r9
	mov	r1, #4
	bl	lua_toboolean(PLT)
	cmp	r0, #0
	bne	.L506
	ldr	r1, .L539
	mov	r0, r7
.LPIC47:
	add	r1, pc, r1
	bl	strpbrk(PLT)
	cmp	r0, #0
	ldrne	r3, [sp, #12]
	bne	.L505
.L506:
	ldr	r8, [sp, #16]
	ldr	r3, [sp]
	cmp	r8, #0
	add	r6, r3, r4
	ldr	r5, [sp, #12]
	beq	.L507
	rsb	r4, r4, r5
	cmp	r4, r8
	bcc	.L508
	sub	r8, r8, #1
	subs	r4, r4, r8
	beq	.L508
	ldrb	fp, [r7], #1	@ zero_extendqisi2
	b	.L509
.L511:
	bl	memcmp(PLT)
	rsb	r3, r5, r6
	mov	r6, r5
	cmp	r0, #0
	beq	.L520
	adds	r4, r4, r3
	beq	.L508
.L509:
	mov	r1, fp
	mov	r2, r4
	mov	r0, r6
	bl	memchr(PLT)
	mov	r1, r7
	mov	r2, r8
	subs	r10, r0, #0
	add	r5, r10, #1
	mov	r0, r5
	bne	.L511
.L508:
	mov	r0, r9
	bl	lua_pushnil(PLT)
	mov	r0, #1
.L512:
	add	sp, sp, #300
	@ sp needed
	ldmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, pc}
.L507:
	cmp	r6, #0
	beq	.L508
.L510:
	ldr	r3, [sp]
	mov	r0, r9
	rsb	r6, r3, r6
	add	r1, r6, #1
	bl	lua_pushinteger(PLT)
	ldr	r1, [sp, #16]
	mov	r0, r9
	add	r1, r6, r1
	bl	lua_pushinteger(PLT)
	mov	r0, #2
	add	sp, sp, #300
	@ sp needed
	ldmfd	sp!, {r4, r5, r6, r7, r8, r9, r10, fp, pc}
.L520:
	mov	r6, r10
	b	.L510
.L505:
	ldrb	r2, [r7]	@ zero_extendqisi2
	add	r6, sp, #20
	cmp	r2, #94
	ldr	r2, [sp]
	moveq	r5, #1
	movne	r5, #0
	add	r3, r2, r3
	addeq	r7, r7, #1
	str	r3, [sp, #24]
	add	r4, r2, r4
	str	r9, [sp, #28]
	str	r2, [sp, #20]
	eor	r5, r5, #1
	mov	fp, #0
	b	.L517
.L514:
	ldr	r3, [sp, #24]
	cmp	r4, r3
	movcs	r4, #0
	andcc	r4, r5, #1
	cmp	r4, #0
	mov	r4, r1
	beq	.L508
.L517:
	mov	r1, r4
	mov	r2, r7
	mov	r0, r6
	str	fp, [sp, #36]
	str	fp, [sp, #32]
	bl	match(PLT)
	add	r1, r4, #1
	subs	r2, r0, #0
	beq	.L514
	cmp	r8, #0
	beq	.L515
	ldr	r5, [sp]
	mov	r0, r9
	rsb	r1, r5, r4
	add	r1, r1, #1
	str	r2, [sp, #4]
	bl	lua_pushinteger(PLT)
	ldr	r2, [sp, #4]
	mov	r0, r9
	rsb	r1, r5, r2
	bl	lua_pushinteger(PLT)
	mov	r1, #0
	mov	r0, r6
	mov	r2, r1
	bl	push_captures(PLT)
	add	r0, r0, #2
	b	.L512
.L515:
	mov	r0, r6
	mov	r1, r4
	bl	push_captures(PLT)
	b	.L512
.L540:
	.align	2
.L539:
	.word	.LC9-(.LPIC47+8)
	.size	str_find_aux, .-str_find_aux
	.align	2
	.type	lj_cf_string_match, %function
lj_cf_string_match:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	@ link register save eliminated.
	mov	r1, #0
	b	str_find_aux(PLT)
	.size	lj_cf_string_match, .-lj_cf_string_match
	.align	2
	.type	lj_cf_string_find, %function
lj_cf_string_find:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	@ link register save eliminated.
	mov	r1, #1
	b	str_find_aux(PLT)
	.size	lj_cf_string_find, .-lj_cf_string_find
	.align	2
	.global	luaopen_string
	.type	luaopen_string, %function
luaopen_string:
	@ args = 0, pretend = 0, frame = 0
	@ frame_needed = 0, uses_anonymous_args = 0
	ldr	r2, .L545
	stmfd	sp!, {r3, r4, r5, lr}
	ldr	r1, .L545+4
	ldr	r3, .L545+8
.LPIC49:
	add	r2, pc, r2
.LPIC50:
	add	r3, pc, r3
	add	r2, r2, #32
.LPIC48:
	add	r1, pc, r1
	mov	r4, r0
	bl	lj_lib_register(PLT)
	ldr	r2, .L545+12
	mov	r0, r4
	mvn	r1, #0
.LPIC51:
	add	r2, pc, r2
	bl	lua_getfield(PLT)
	ldr	r2, .L545+16
	mov	r0, r4
	mvn	r1, #1
.LPIC52:
	add	r2, pc, r2
	bl	lua_setfield(PLT)
	mov	r0, r4
	mov	r1, #0
	mov	r2, #1
	bl	lj_tab_new(PLT)
	ldr	r3, [r4, #8]
	ldr	r2, [r3, #228]
	mov	r5, r0
	str	r5, [r3, #332]
	mov	r1, r5
	mov	r0, r4
	bl	lj_tab_setstr(PLT)
	ldr	r3, [r4, #20]
	mvn	r2, #11
	ldr	r1, [r3, #-8]
	mvn	r3, #1
	stmia	r0, {r1, r2}
	mov	r0, #1
	strb	r3, [r5, #6]
	ldmfd	sp!, {r3, r4, r5, pc}
.L546:
	.align	2
.L545:
	.word	.LANCHOR0-(.LPIC49+8)
	.word	.LC10-(.LPIC48+8)
	.word	.LANCHOR1-(.LPIC50+8)
	.word	.LC11-(.LPIC51+8)
	.word	.LC12-(.LPIC52+8)
	.size	luaopen_string, .-luaopen_string
	.section	.rodata
	.align	2
.LANCHOR0 = . + 0
	.type	match_class_map, %object
	.size	match_class_map, 32
match_class_map:
	.byte	0
	.byte	96
	.byte	0
	.byte	1
	.byte	8
	.byte	0
	.byte	0
	.byte	108
	.byte	0
	.byte	0
	.byte	0
	.byte	0
	.byte	64
	.byte	0
	.byte	0
	.byte	0
	.byte	4
	.byte	0
	.byte	0
	.byte	2
	.byte	0
	.byte	32
	.byte	0
	.byte	104
	.byte	16
	.byte	0
	.byte	0
	.byte	0
	.byte	0
	.byte	0
	.byte	0
	.byte	0
	.type	lj_lib_init_string, %object
	.size	lj_lib_init_string, 82
lj_lib_init_string:
	.byte	77
	.byte	53
	.byte	14
	.byte	67
	.byte	108
	.byte	101
	.byte	110
	.byte	68
	.byte	98
	.byte	121
	.byte	116
	.byte	101
	.byte	68
	.byte	99
	.byte	104
	.byte	97
	.byte	114
	.byte	67
	.byte	115
	.byte	117
	.byte	98
	.byte	67
	.byte	114
	.byte	101
	.byte	112
	.byte	71
	.byte	114
	.byte	101
	.byte	118
	.byte	101
	.byte	114
	.byte	115
	.byte	101
	.byte	-123
	.byte	108
	.byte	111
	.byte	119
	.byte	101
	.byte	114
	.byte	-123
	.byte	117
	.byte	112
	.byte	112
	.byte	101
	.byte	114
	.byte	4
	.byte	100
	.byte	117
	.byte	109
	.byte	112
	.byte	4
	.byte	102
	.byte	105
	.byte	110
	.byte	100
	.byte	5
	.byte	109
	.byte	97
	.byte	116
	.byte	99
	.byte	104
	.byte	-2
	.byte	6
	.byte	103
	.byte	109
	.byte	97
	.byte	116
	.byte	99
	.byte	104
	.byte	4
	.byte	103
	.byte	115
	.byte	117
	.byte	98
	.byte	6
	.byte	102
	.byte	111
	.byte	114
	.byte	109
	.byte	97
	.byte	116
	.byte	-1
	.section	.data.rel.ro.local,"aw",%progbits
	.align	2
.LANCHOR1 = . + 0
	.type	lj_lib_cf_string, %object
	.size	lj_lib_cf_string, 48
lj_lib_cf_string:
	.word	lj_ffh_string_len
	.word	lj_ffh_string_byte
	.word	lj_ffh_string_char
	.word	lj_ffh_string_sub
	.word	lj_ffh_string_rep
	.word	lj_ffh_string_reverse
	.word	lj_cf_string_dump
	.word	lj_cf_string_find
	.word	lj_cf_string_match
	.word	lj_cf_string_gmatch
	.word	lj_cf_string_gsub
	.word	lj_cf_string_format
	.section	.rodata.str1.4,"aMS",%progbits,1
	.align	2
.LC0:
	.ascii	"l\000"
	.space	2
.LC1:
	.ascii	"-+ #0\000"
	.space	2
.LC2:
	.ascii	"%p\000"
	.space	1
.LC3:
	.ascii	"nil\000"
.LC4:
	.ascii	"false\000"
	.space	2
.LC5:
	.ascii	"true\000"
	.space	3
.LC6:
	.ascii	"function: builtin#%d\000"
	.space	3
.LC7:
	.ascii	"%s: %p\000"
	.space	1
.LC8:
	.ascii	"too many captures\000"
	.space	2
.LC9:
	.ascii	"^$*+?.([%-\000"
	.space	1
.LC10:
	.ascii	"string\000"
	.space	1
.LC11:
	.ascii	"gmatch\000"
	.space	1
.LC12:
	.ascii	"gfind\000"
	.hidden	lj_tab_setstr
	.hidden	lj_tab_new
	.hidden	lj_lib_register
	.hidden	lj_state_growstack
	.hidden	lj_lib_optint
	.hidden	lj_lib_optstr
	.hidden	lj_err_arg
	.hidden	lj_str_needbuf
	.hidden	lj_bcwrite
	.hidden	lj_lib_checkfunc
	.hidden	lj_obj_itypename
	.hidden	lj_obj_typename
	.hidden	lj_char_bits
	.hidden	lj_err_caller
	.hidden	lj_str_new
	.hidden	lj_str_fromnumber
	.hidden	lj_meta_lookup
	.hidden	lj_str_bufnum
	.hidden	lj_err_callerv
	.hidden	lj_lib_checknum
	.hidden	lj_lib_checkint
	.hidden	lj_str_pushf
	.hidden	lj_lib_checkbit
	.hidden	lj_lib_checkstr
	.ident	"GCC: (GNU) 4.9.x 20150123 (prerelease)"
	.section	.note.GNU-stack,"",%progbits
