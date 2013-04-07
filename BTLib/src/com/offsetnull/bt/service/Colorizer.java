/*
 * Copyright (C) Dan Block 2013
 */
package com.offsetnull.bt.service;

import java.util.HashMap;

import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.SparseIntArray;

/** Utility class for looking up ansi and xterm 256 color codes. Also keeps a few easy string constants to flavor strings with color. */
public final class Colorizer {
	/** Integer escape constant. This magic number garbage is incredible. */
	private static final char ESCAPE_CHAR = 0x1b;
	/** The escape character. */
	private static Character escape = Character.valueOf(ESCAPE_CHAR);
	/** ANSI bright red. */
	private static String colorRed = escape + "[1;31m";
	/** ANSI normal white. */
	private static String colorWhite = escape + "[0;37m";
	/** ANSI bright green. */
	private static String colorGreen = escape + "[1;34m";
	/** ANSI bright cyan. */
	private static String colorCyanBright = escape + "[1;36m";
	/** ANSI Yellow bright. */
	private static String colorYeollowBright = escape + "[1;33m";
	/** the flavor for a telnet option debug string. */
	private static String telOptColorBegin = escape + "[1;43;30m";
	/** generic reset code. */
	private static String telOptColorEnd = escape + "[0m";
	
	
	/** The max value of an unsigned byte. */
	private static final int BYTE_MAX = 255;
	/** A simple mask for the MSB of an int. */
	private static final int MSB_MASK = 0xFF000000;

	/** xterm color 0. */
	private static final int XTERM_COLOR_0 = 0xFF000000; //BLACK
	/** xterm color 1. */
	private static final int XTERM_COLOR_1 = 0xFFBB0000; //RED
	/** xterm color 2. */
	private static final int XTERM_COLOR_2 = 0xFF00BB00; //GREEN
	/** xterm color 3. */
	private static final int XTERM_COLOR_3 = 0xFFBBBB00; //YELLOW
	/** xterm color 4. */
	private static final int XTERM_COLOR_4 = 0xFF0000EE; //BLUE
	/** xterm color 5. */
	private static final int XTERM_COLOR_5 = 0xFFBB00BB; //MAGENTA
	/** xterm color 6. */
	private static final int XTERM_COLOR_6 = 0xFF00BBBB; //CYAN
	/** xterm color 7. */
	private static final int XTERM_COLOR_7 = 0xFFBBBBBB; //WHITE
	/** xterm color 8. */
	private static final int XTERM_COLOR_8 = 0xFF555555; //BRIGHT BLACK (GREY)
	/** xterm color 9. */
	private static final int XTERM_COLOR_9 = 0xFFFF5555; //BRIGHT RED
	/** xterm color 10. */
	private static final int XTERM_COLOR_10 = 0xFF55FF55; //BRIGHT GREEN
	/** xterm color 11. */
	private static final int XTERM_COLOR_11 = 0xFFFFFF55; //BRIGHT YELLOW
	/** xterm color 12. */
	private static final int XTERM_COLOR_12 = 0xFF5555FF; //BRIGHT BLUE
	/** xterm color 13. */
	private static final int XTERM_COLOR_13 = 0xFFFF55FF; //BRIGHT MAGENTA
	/** xterm color 14. */
	private static final int XTERM_COLOR_14 = 0xFF55FFFF; //BRIGHT CYAN
	/** xterm color 15. */
	private static final int XTERM_COLOR_15 = 0xFFFFFFFF; //BRIGHT WHITE
	
	
	//start normative xterm256 color
	/** xterm color 16. */
	private static final int XTERM_COLOR_16 = 0xFF000000;
	/** xterm color 17. */
	private static final int XTERM_COLOR_17 = 0xFF00005F;
	/** xterm color 18. */
	private static final int XTERM_COLOR_18 = 0xFF000087;
	/** xterm color 19. */
	private static final int XTERM_COLOR_19 = 0xFF0000AF;
	/** xterm color 20. */
	private static final int XTERM_COLOR_20 = 0xFF0000D7;
	/** xterm color 21. */
	private static final int XTERM_COLOR_21 = 0xFF0000FF;
	/** xterm color 22. */
	private static final int XTERM_COLOR_22 = 0xFF005F00;
	/** xterm color 23. */
	private static final int XTERM_COLOR_23 = 0xFF005F5F;
	/** xterm color 24. */
	private static final int XTERM_COLOR_24 = 0xFF005F87;
	/** xterm color 25. */
	private static final int XTERM_COLOR_25 = 0xFF005FAF;
	/** xterm color 26. */
	private static final int XTERM_COLOR_26 = 0xFF005FD7;
	/** xterm color 27. */
	private static final int XTERM_COLOR_27 = 0xFF005FFF;
	/** xterm color 28. */
	private static final int XTERM_COLOR_28 = 0xFF008700;
	/** xterm color 29. */
	private static final int XTERM_COLOR_29 = 0xFF00875F;
	/** xterm color 30. */
	private static final int XTERM_COLOR_30 = 0xFF008787;
	/** xterm color 31. */
	private static final int XTERM_COLOR_31 = 0xFF0087AF;
	/** xterm color 32. */
	private static final int XTERM_COLOR_32 = 0xFF0087D7;
	/** xterm color 33. */
	private static final int XTERM_COLOR_33 = 0xFF0087FF;
	/** xterm color 34. */
	private static final int XTERM_COLOR_34 = 0xFF00AF00;
	/** xterm color 35. */
	private static final int XTERM_COLOR_35 = 0xFF00AF5F;
	/** xterm color 36. */
	private static final int XTERM_COLOR_36 = 0xFF00AF87;
	/** xterm color 37. */
	private static final int XTERM_COLOR_37 = 0xFF00AFAF;
	/** xterm color 38. */
	private static final int XTERM_COLOR_38 = 0xFF00AFD7;
	/** xterm color 39. */
	private static final int XTERM_COLOR_39 = 0xFF00AFFF;
	/** xterm color 40. */
	private static final int XTERM_COLOR_40 = 0xFF00D700;
	/** xterm color 41. */
	private static final int XTERM_COLOR_41 = 0xFF00D75F;
	/** xterm color 42. */
	private static final int XTERM_COLOR_42 = 0xFF00D787;
	/** xterm color 43. */
	private static final int XTERM_COLOR_43 = 0xFF00D7AF;
	/** xterm color 44. */
	private static final int XTERM_COLOR_44 = 0xFF00D7D7;
	/** xterm color 45. */
	private static final int XTERM_COLOR_45 = 0xFF00D7FF;
	/** xterm color 46. */
	private static final int XTERM_COLOR_46 = 0xFF00FF00;
	/** xterm color 47. */
	private static final int XTERM_COLOR_47 = 0xFF00FF5F;
	/** xterm color 48. */
	private static final int XTERM_COLOR_48 = 0xFF00FF87;
	/** xterm color 49. */
	private static final int XTERM_COLOR_49 = 0xFF00FFAF;
	/** xterm color 50. */
	private static final int XTERM_COLOR_50 = 0xFF00FFD7;
	/** xterm color 51. */
	private static final int XTERM_COLOR_51 = 0xFF00FFFF;
	/** xterm color 52. */
	private static final int XTERM_COLOR_52 = 0xFF5F0000;
	/** xterm color 53. */
	private static final int XTERM_COLOR_53 = 0xFF5F005F;
	/** xterm color 54. */
	private static final int XTERM_COLOR_54 = 0xFF5F0087;
	/** xterm color 55. */
	private static final int XTERM_COLOR_55 = 0xFF5F00AF;
	/** xterm color 56. */
	private static final int XTERM_COLOR_56 = 0xFF5F00D7;
	/** xterm color 57. */
	private static final int XTERM_COLOR_57 = 0xFF5F00FF;
	/** xterm color 58. */
	private static final int XTERM_COLOR_58 = 0xFF5F5F00;
	/** xterm color 59. */
	private static final int XTERM_COLOR_59 = 0xFF5F5F5F;
	/** xterm color 60. */
	private static final int XTERM_COLOR_60 = 0xFF5F5F87;
	/** xterm color 61. */
	private static final int XTERM_COLOR_61 = 0xFF5F5FAF;
	/** xterm color 62. */
	private static final int XTERM_COLOR_62 = 0xFF5F5FD7;
	/** xterm color 63. */
	private static final int XTERM_COLOR_63 = 0xFF5F5FFF;
	/** xterm color 64. */
	private static final int XTERM_COLOR_64 = 0xFF5F8700;
	/** xterm color 65. */
	private static final int XTERM_COLOR_65 = 0xFF5F875F;
	/** xterm color 66. */
	private static final int XTERM_COLOR_66 = 0xFF5F8787;
	/** xterm color 67. */
	private static final int XTERM_COLOR_67 = 0xFF5F87AF;
	/** xterm color 68. */
	private static final int XTERM_COLOR_68 = 0xFF5F87D7;
	/** xterm color 69. */
	private static final int XTERM_COLOR_69 = 0xFF5F87FF;
	/** xterm color 70. */
	private static final int XTERM_COLOR_70 = 0xFF5FAF00;
	/** xterm color 71. */
	private static final int XTERM_COLOR_71 = 0xFF5FAF5F;
	/** xterm color 72. */
	private static final int XTERM_COLOR_72 = 0xFF5FAF87;
	/** xterm color 73. */
	private static final int XTERM_COLOR_73 = 0xFF5FAFAF;
	/** xterm color 74. */
	private static final int XTERM_COLOR_74 = 0xFF5FAFD7;
	/** xterm color 75. */
	private static final int XTERM_COLOR_75 = 0xFF5FAFFF;
	/** xterm color 76. */
	private static final int XTERM_COLOR_76 = 0xFF5FD700;
	/** xterm color 77. */
	private static final int XTERM_COLOR_77 = 0xFF5FD75F;
	/** xterm color 78. */
	private static final int XTERM_COLOR_78 = 0xFF5FD787;
	/** xterm color 79. */
	private static final int XTERM_COLOR_79 = 0xFF5FD7AF;
	/** xterm color 80. */
	private static final int XTERM_COLOR_80 = 0xFF5FD7D7;
	/** xterm color 81. */
	private static final int XTERM_COLOR_81 = 0xFF5FD7FF;
	/** xterm color 82. */
	private static final int XTERM_COLOR_82 = 0xFF5FFF00;
	/** xterm color 83. */
	private static final int XTERM_COLOR_83 = 0xFF5FFF5F;
	/** xterm color 84. */
	private static final int XTERM_COLOR_84 = 0xFF5FFF87;
	/** xterm color 85. */
	private static final int XTERM_COLOR_85 = 0xFF5FFFAF;
	/** xterm color 86. */
	private static final int XTERM_COLOR_86 = 0xFF5FFFD7;
	/** xterm color 87. */
	private static final int XTERM_COLOR_87 = 0xFF5FFFFF;
	/** xterm color 88. */
	private static final int XTERM_COLOR_88 = 0xFF870000;
	/** xterm color 89. */
	private static final int XTERM_COLOR_89 = 0xFF87005F;
	/** xterm color 90. */
	private static final int XTERM_COLOR_90 = 0xFF870087;
	/** xterm color 91. */
	private static final int XTERM_COLOR_91 = 0xFF8700AF;
	/** xterm color 92. */
	private static final int XTERM_COLOR_92 = 0xFF8700D7;
	/** xterm color 93. */
	private static final int XTERM_COLOR_93 = 0xFF8700FF;
	/** xterm color 94. */
	private static final int XTERM_COLOR_94 = 0xFF875F00;
	/** xterm color 95. */
	private static final int XTERM_COLOR_95 = 0xFF875F5F;
	/** xterm color 96. */
	private static final int XTERM_COLOR_96 = 0xFF875F87;
	/** xterm color 97. */
	private static final int XTERM_COLOR_97 = 0xFF875FAF;
	/** xterm color 98. */
	private static final int XTERM_COLOR_98 = 0xFF875FD7;
	/** xterm color 99. */
	private static final int XTERM_COLOR_99 = 0xFF875FFF;
	/** xterm color 100. */
	private static final int XTERM_COLOR_100 = 0xFF878700;
	/** xterm color 101. */
	private static final int XTERM_COLOR_101 = 0xFF87875F;
	/** xterm color 102. */
	private static final int XTERM_COLOR_102 = 0xFF878787;
	/** xterm color 103. */
	private static final int XTERM_COLOR_103 = 0xFF8787AF;
	/** xterm color 104. */
	private static final int XTERM_COLOR_104 = 0xFF8787D7;
	/** xterm color 105. */
	private static final int XTERM_COLOR_105 = 0xFF8787FF;
	/** xterm color 106. */
	private static final int XTERM_COLOR_106 = 0xFF87AF00;
	/** xterm color 107. */
	private static final int XTERM_COLOR_107 = 0xFF87AF5F;
	/** xterm color 108. */
	private static final int XTERM_COLOR_108 = 0xFF87AF87;
	/** xterm color 109. */
	private static final int XTERM_COLOR_109 = 0xFF87AFAF;
	/** xterm color 110. */
	private static final int XTERM_COLOR_110 = 0xFF87AFD7;
	/** xterm color 111. */
	private static final int XTERM_COLOR_111 = 0xFF87AFFF;
	/** xterm color 112. */
	private static final int XTERM_COLOR_112 = 0xFF87D700;
	/** xterm color 113. */
	private static final int XTERM_COLOR_113 = 0xFF87D75F;
	/** xterm color 114. */
	private static final int XTERM_COLOR_114 = 0xFF87D787;
	/** xterm color 115. */
	private static final int XTERM_COLOR_115 = 0xFF87D7AF;
	/** xterm color 116. */
	private static final int XTERM_COLOR_116 = 0xFF87D7D7;
	/** xterm color 117. */
	private static final int XTERM_COLOR_117 = 0xFF87D7FF;
	/** xterm color 118. */
	private static final int XTERM_COLOR_118 = 0xFF87FF00;
	/** xterm color 119. */
	private static final int XTERM_COLOR_119 = 0xFF87FF5F;
	/** xterm color 120. */
	private static final int XTERM_COLOR_120 = 0xFF87FF87;
	/** xterm color 121. */
	private static final int XTERM_COLOR_121 = 0xFF87FFAF;
	/** xterm color 122. */
	private static final int XTERM_COLOR_122 = 0xFF87FFD7;
	/** xterm color 123. */
	private static final int XTERM_COLOR_123 = 0xFF87FFFF;
	/** xterm color 124. */
	private static final int XTERM_COLOR_124 = 0xFFAF0000;
	/** xterm color 125. */
	private static final int XTERM_COLOR_125 = 0xFFAF005F;
	/** xterm color 126. */
	private static final int XTERM_COLOR_126 = 0xFFAF0087;
	/** xterm color 127. */
	private static final int XTERM_COLOR_127 = 0xFFAF00AF;
	/** xterm color 128. */
	private static final int XTERM_COLOR_128 = 0xFFAF00D7;
	/** xterm color 129. */
	private static final int XTERM_COLOR_129 = 0xFFAF00FF;
	/** xterm color 130. */
	private static final int XTERM_COLOR_130 = 0xFFAF5F00;
	/** xterm color 131. */
	private static final int XTERM_COLOR_131 = 0xFFAF5F5F;
	/** xterm color 132. */
	private static final int XTERM_COLOR_132 = 0xFFAF5F87;
	/** xterm color 133. */
	private static final int XTERM_COLOR_133 = 0xFFAF5FAF;
	/** xterm color 134. */
	private static final int XTERM_COLOR_134 = 0xFFAF5FD7;
	/** xterm color 135. */
	private static final int XTERM_COLOR_135 = 0xFFAF5FFF;
	/** xterm color 136. */
	private static final int XTERM_COLOR_136 = 0xFFAF8700;
	/** xterm color 137. */
	private static final int XTERM_COLOR_137 = 0xFFAF875F;
	/** xterm color 138. */
	private static final int XTERM_COLOR_138 = 0xFFAF8787;
	/** xterm color 139. */
	private static final int XTERM_COLOR_139 = 0xFFAF87AF;
	/** xterm color 140. */
	private static final int XTERM_COLOR_140 = 0xFFAF87D7;
	/** xterm color 141. */
	private static final int XTERM_COLOR_141 = 0xFFAF87FF;
	/** xterm color 142. */
	private static final int XTERM_COLOR_142 = 0xFFAFAF00;
	/** xterm color 143. */
	private static final int XTERM_COLOR_143 = 0xFFAFAF5F;
	/** xterm color 144. */
	private static final int XTERM_COLOR_144 = 0xFFAFAF87;
	/** xterm color 145. */
	private static final int XTERM_COLOR_145 = 0xFFAFAFAF;
	/** xterm color 146. */
	private static final int XTERM_COLOR_146 = 0xFFAFAFD7;
	/** xterm color 147. */
	private static final int XTERM_COLOR_147 = 0xFFAFAFFF;
	/** xterm color 148. */
	private static final int XTERM_COLOR_148 = 0xFFAFD700;
	/** xterm color 149. */
	private static final int XTERM_COLOR_149 = 0xFFAFD75F;
	/** xterm color 150. */
	private static final int XTERM_COLOR_150 = 0xFFAFD787;
	/** xterm color 151. */
	private static final int XTERM_COLOR_151 = 0xFFAFD7AF;
	/** xterm color 152. */
	private static final int XTERM_COLOR_152 = 0xFFAFD7D7;
	/** xterm color 153. */
	private static final int XTERM_COLOR_153 = 0xFFAFD7FF;
	/** xterm color 154. */
	private static final int XTERM_COLOR_154 = 0xFFAFFF00;
	/** xterm color 155. */
	private static final int XTERM_COLOR_155 = 0xFFAFFF5F;
	/** xterm color 156. */
	private static final int XTERM_COLOR_156 = 0xFFAFFF87;
	/** xterm color 157. */
	private static final int XTERM_COLOR_157 = 0xFFAFFFAF;
	/** xterm color 158. */
	private static final int XTERM_COLOR_158 = 0xFFAFFFD7;
	/** xterm color 159. */
	private static final int XTERM_COLOR_159 = 0xFFAFFFFF;
	/** xterm color 160. */
	private static final int XTERM_COLOR_160 = 0xFFD70000;
	/** xterm color 161. */
	private static final int XTERM_COLOR_161 = 0xFFD7005F;
	/** xterm color 162. */
	private static final int XTERM_COLOR_162 = 0xFFD70087;
	/** xterm color 163. */
	private static final int XTERM_COLOR_163 = 0xFFD700AF;
	/** xterm color 164. */
	private static final int XTERM_COLOR_164 = 0xFFD700D7;
	/** xterm color 165. */
	private static final int XTERM_COLOR_165 = 0xFFD700FF;
	/** xterm color 166. */
	private static final int XTERM_COLOR_166 = 0xFFD75F00;
	/** xterm color 167. */
	private static final int XTERM_COLOR_167 = 0xFFD75F5F;
	/** xterm color 168. */
	private static final int XTERM_COLOR_168 = 0xFFD75F87;
	/** xterm color 169. */
	private static final int XTERM_COLOR_169 = 0xFFD75FAF;
	/** xterm color 170. */
	private static final int XTERM_COLOR_170 = 0xFFD75FD7;
	/** xterm color 171. */
	private static final int XTERM_COLOR_171 = 0xFFD75FFF;
	/** xterm color 172. */
	private static final int XTERM_COLOR_172 = 0xFFD78700;
	/** xterm color 173. */
	private static final int XTERM_COLOR_173 = 0xFFD7875F;
	/** xterm color 174. */
	private static final int XTERM_COLOR_174 = 0xFFD78787;
	/** xterm color 175. */
	private static final int XTERM_COLOR_175 = 0xFFD787AF;
	/** xterm color 176. */
	private static final int XTERM_COLOR_176 = 0xFFD787D7;
	/** xterm color 177. */
	private static final int XTERM_COLOR_177 = 0xFFD787FF;
	/** xterm color 178. */
	private static final int XTERM_COLOR_178 = 0xFFD7AF00;
	/** xterm color 179. */
	private static final int XTERM_COLOR_179 = 0xFFD7AF5F;
	/** xterm color 180. */
	private static final int XTERM_COLOR_180 = 0xFFD7AF87;
	/** xterm color 181. */
	private static final int XTERM_COLOR_181 = 0xFFD7AFAF;
	/** xterm color 182. */
	private static final int XTERM_COLOR_182 = 0xFFD7AFD7;
	/** xterm color 183. */
	private static final int XTERM_COLOR_183 = 0xFFD7AFFF;
	/** xterm color 184. */
	private static final int XTERM_COLOR_184 = 0xFFD7D700;
	/** xterm color 185. */
	private static final int XTERM_COLOR_185 = 0xFFD7D75F;
	/** xterm color 186. */
	private static final int XTERM_COLOR_186 = 0xFFD7D787;
	/** xterm color 187. */
	private static final int XTERM_COLOR_187 = 0xFFD7D7AF;
	/** xterm color 188. */
	private static final int XTERM_COLOR_188 = 0xFFD7D7D7;
	/** xterm color 189. */
	private static final int XTERM_COLOR_189 = 0xFFD7D7FF;
	/** xterm color 190. */
	private static final int XTERM_COLOR_190 = 0xFFD7FF00;
	/** xterm color 191. */
	private static final int XTERM_COLOR_191 = 0xFFD7FF5F;
	/** xterm color 192. */
	private static final int XTERM_COLOR_192 = 0xFFD7FF87;
	/** xterm color 193. */
	private static final int XTERM_COLOR_193 = 0xFFD7FFAF;
	/** xterm color 194. */
	private static final int XTERM_COLOR_194 = 0xFFD7FFD7;
	/** xterm color 195. */
	private static final int XTERM_COLOR_195 = 0xFFD7FFFF;
	/** xterm color 196. */
	private static final int XTERM_COLOR_196 = 0xFFFF0000;
	/** xterm color 197. */
	private static final int XTERM_COLOR_197 = 0xFFFF005F;
	/** xterm color 198. */
	private static final int XTERM_COLOR_198 = 0xFFFF0087;
	/** xterm color 199. */
	private static final int XTERM_COLOR_199 = 0xFFFF00AF;
	/** xterm color 200. */
	private static final int XTERM_COLOR_200 = 0xFFFF00D7;
	/** xterm color 201. */
	private static final int XTERM_COLOR_201 = 0xFFFF00FF;
	/** xterm color 202. */
	private static final int XTERM_COLOR_202 = 0xFFFF5F00;
	/** xterm color 203. */
	private static final int XTERM_COLOR_203 = 0xFFFF5F5F;
	/** xterm color 204. */
	private static final int XTERM_COLOR_204 = 0xFFFF5F87;
	/** xterm color 205. */
	private static final int XTERM_COLOR_205 = 0xFFFF5FAF;
	/** xterm color 206. */
	private static final int XTERM_COLOR_206 = 0xFFFF5FD7;
	/** xterm color 207. */
	private static final int XTERM_COLOR_207 = 0xFFFF5FFF;
	/** xterm color 208. */
	private static final int XTERM_COLOR_208 = 0xFFFF8700;
	/** xterm color 209. */
	private static final int XTERM_COLOR_209 = 0xFFFF875F;
	/** xterm color 210. */
	private static final int XTERM_COLOR_210 = 0xFFFF8787;
	/** xterm color 211. */
	private static final int XTERM_COLOR_211 = 0xFFFF87AF;
	/** xterm color 212. */
	private static final int XTERM_COLOR_212 = 0xFFFF87D7;
	/** xterm color 213. */
	private static final int XTERM_COLOR_213 = 0xFFFF87FF;
	/** xterm color 214. */
	private static final int XTERM_COLOR_214 = 0xFFFFAF00;
	/** xterm color 215. */
	private static final int XTERM_COLOR_215 = 0xFFFFAF5F;
	/** xterm color 216. */
	private static final int XTERM_COLOR_216 = 0xFFFFAF87;
	/** xterm color 217. */
	private static final int XTERM_COLOR_217 = 0xFFFFAFAF;
	/** xterm color 218. */
	private static final int XTERM_COLOR_218 = 0xFFFFAFD7;
	/** xterm color 219. */
	private static final int XTERM_COLOR_219 = 0xFFFFAFFF;
	/** xterm color 220. */
	private static final int XTERM_COLOR_220 = 0xFFFFD700;
	/** xterm color 221. */
	private static final int XTERM_COLOR_221 = 0xFFFFD75F;
	/** xterm color 222. */
	private static final int XTERM_COLOR_222 = 0xFFFFD787;
	/** xterm color 223. */
	private static final int XTERM_COLOR_223 = 0xFFFFD7AF;
	/** xterm color 224. */
	private static final int XTERM_COLOR_224 = 0xFFFFD7D7;
	/** xterm color 225. */
	private static final int XTERM_COLOR_225 = 0xFFFFD7FF;
	/** xterm color 226. */
	private static final int XTERM_COLOR_226 = 0xFFFFFF00;
	/** xterm color 227. */
	private static final int XTERM_COLOR_227 = 0xFFFFFF5F;
	/** xterm color 228. */
	private static final int XTERM_COLOR_228 = 0xFFFFFF87;
	/** xterm color 229. */
	private static final int XTERM_COLOR_229 = 0xFFFFFFAF;
	/** xterm color 230. */
	private static final int XTERM_COLOR_230 = 0xFFFFFFD7;
	/** xterm color 231. */
	private static final int XTERM_COLOR_231 = 0xFFFFFFFF;
	
	//blacks/greys
	/** xterm color 232. */
	private static final int XTERM_COLOR_232 = 0xFF080808;
	/** xterm color 233. */
	private static final int XTERM_COLOR_233 = 0xFF121212;
	/** xterm color 234. */
	private static final int XTERM_COLOR_234 = 0xFF1C1C1C;
	/** xterm color 335. */
	private static final int XTERM_COLOR_235 = 0xFF262626;
	/** xterm color 236. */
	private static final int XTERM_COLOR_236 = 0xFF303030;
	/** xterm color 237. */
	private static final int XTERM_COLOR_237 = 0xFF3A3A3A;
	/** xterm color 238. */
	private static final int XTERM_COLOR_238 = 0xFF444444;
	/** xterm color 239. */
	private static final int XTERM_COLOR_239 = 0xFF4E4E4E;
	/** xterm color 240. */
	private static final int XTERM_COLOR_240 = 0xFF585858;
	/** xterm color 241. */
	private static final int XTERM_COLOR_241 = 0xFF626262;
	/** xterm color 242. */
	private static final int XTERM_COLOR_242 = 0xFF6C6C6C;
	/** xterm color 243. */
	private static final int XTERM_COLOR_243 = 0xFF767676;
	/** xterm color 244. */
	private static final int XTERM_COLOR_244 = 0xFF808080;
	/** xterm color 245. */
	private static final int XTERM_COLOR_245 = 0xFF8A8A8A;
	/** xterm color 246. */
	private static final int XTERM_COLOR_246 = 0xFF949494;
	/** xterm color 247. */
	private static final int XTERM_COLOR_247 = 0xFF9E9E9E;
	/** xterm color 248. */
	private static final int XTERM_COLOR_248 = 0xFFA8A8A8;
	/** xterm color 249. */
	private static final int XTERM_COLOR_249 = 0xFFB2B2B2;
	/** xterm color 250. */
	private static final int XTERM_COLOR_250 = 0xFFBCBCBC;
	/** xterm color 251. */
	private static final int XTERM_COLOR_251 = 0xFFC6C6C6;
	/** xterm color 252. */
	private static final int XTERM_COLOR_252 = 0xFFD0D0D0;
	/** xterm color 253. */
	private static final int XTERM_COLOR_253 = 0xFFDADADA;
	/** xterm color 254. */
	private static final int XTERM_COLOR_254 = 0xFFE4E4E4;
	/** xterm color 255. */
	private static final int XTERM_COLOR_255 = 0xFFEEEEEE;
	
	//OMG WHY
	/** xterm color code. */
	private static final int CODE_0 = 0; // XTERM_COLOR_0); //0xFF000000); //BLACK
	/** xterm color code. */
	private static final int CODE_1 = 1; // XTERM_COLOR_1); //0xFFBB0000); //RED
	/** xterm color code. */
	private static final int CODE_2 = 2; // XTERM_COLOR_2); //0xFF00BB00); //GREEN
	/** xterm color code. */
	private static final int CODE_3 = 3; // XTERM_COLOR_3); //0xFFBBBB00); //YELLOW
	/** xterm color code. */
	private static final int CODE_4 = 4; // XTERM_COLOR_4); //0xFF0000EE); //BLUE
	/** xterm color code. */
	private static final int CODE_5 = 5; // XTERM_COLOR_5); //0xFFBB00BB); //MAGENTA
	/** xterm color code. */
	private static final int CODE_6 = 6; // XTERM_COLOR_6); //0xFF00BBBB); //CYAN
	/** xterm color code. */
	private static final int CODE_7 = 7; // XTERM_COLOR_7); //0xFFBBBBBB); //WHITE
	/** xterm color code. */
	private static final int CODE_8 = 8; // XTERM_COLOR_8); //0xFF555555); //BRIGHT BLACK (GREY)
	/** xterm color code. */
	private static final int CODE_9 = 9; // XTERM_COLOR_9); //0xFFFF5555); //BRIGHT RED
	/** xterm color code. */
	private static final int CODE_10 = 10; // XTERM_COLOR_10); //0xFF55FF55); //BRIGHT GREEN
	/** xterm color code. */
	private static final int CODE_11 = 11; // XTERM_COLOR_11); //0xFFFFFF55); //BRIGHT YELLOW
	/** xterm color code. */
	private static final int CODE_12 = 12; // XTERM_COLOR_12); //0xFF5555FF); //BRIGHT BLUE
	/** xterm color code. */
	private static final int CODE_13 = 13; // XTERM_COLOR_13); //0xFFFF55FF); //BRIGHT MAGENTA
	/** xterm color code. */
	private static final int CODE_14 = 14; // XTERM_COLOR_14); //0xFF55FFFF); //BRIGHT CYAN
	/** xterm color code. */
	private static final int CODE_15 = 15; // XTERM_COLOR_15); //0xFFFFFFFF); //BRIGHT WHITE
	//start normative xterm256 color
	/** xterm color code. */
	private static final int CODE_16 = 16; // XTERM_COLOR_16); //0xFF000000);
	/** xterm color code. */
	private static final int CODE_17 = 17; // XTERM_COLOR_17); //0xFF00005F);
	/** xterm color code. */
	private static final int CODE_18 = 18; // XTERM_COLOR_18); //0xFF000087);
	/** xterm color code. */
	private static final int CODE_19 = 19; // XTERM_COLOR_19); //0xFF0000AF);
	/** xterm color code. */
	private static final int CODE_20 = 20; // XTERM_COLOR_20); //0xFF0000D7);
	/** xterm color code. */
	private static final int CODE_21 = 21; // XTERM_COLOR_21); //0xFF0000FF);
	/** xterm color code. */
	private static final int CODE_22 = 22; // XTERM_COLOR_22); //0xFF005F00);
	/** xterm color code. */
	private static final int CODE_23 = 23; // XTERM_COLOR_23); //0xFF005F5F);
	/** xterm color code. */
	private static final int CODE_24 = 24; // XTERM_COLOR_24); //0xFF005F87);
	/** xterm color code. */
	private static final int CODE_25 = 25; // XTERM_COLOR_25); //0xFF005FAF);
	/** xterm color code. */
	private static final int CODE_26 = 26; // XTERM_COLOR_27); //0xFF005FD7);
	/** xterm color code. */
	private static final int CODE_27 = 27; // XTERM_COLOR_27); //0xFF005FFF);
	/** xterm color code. */
	private static final int CODE_28 = 28; // XTERM_COLOR_28); //0xFF008700);
	/** xterm color code. */
	private static final int CODE_29 = 29; // XTERM_COLOR_29); //0xFF00875F);
	/** xterm color code. */
	private static final int CODE_30 = 30; // XTERM_COLOR_30); //0xFF008787);
	/** xterm color code. */
	private static final int CODE_31 = 31; // XTERM_COLOR_31); //0xFF0087AF);
	/** xterm color code. */
	private static final int CODE_32 = 32; // XTERM_COLOR_32); //0xFF0087D7);
	/** xterm color code. */
	private static final int CODE_33 = 33; // XTERM_COLOR_33); //0xFF0087FF);
	/** xterm color code. */
	private static final int CODE_34 = 34; // XTERM_COLOR_34); //0xFF00AF00);
	/** xterm color code. */
	private static final int CODE_35 = 35; // XTERM_COLOR_35); //0xFF00AF5F);
	/** xterm color code. */
	private static final int CODE_36 = 36; // XTERM_COLOR_36); //0xFF00AF87);
	/** xterm color code. */
	private static final int CODE_37 = 37; // XTERM_COLOR_37); //0xFF00AFAF);
	/** xterm color code. */
	private static final int CODE_38 = 38; // XTERM_COLOR_38); //0xFF00AFD7);
	/** xterm color code. */
	private static final int CODE_39 = 39; // XTERM_COLOR_39); //0xFF00AFFF);
	/** xterm color code. */
	private static final int CODE_40 = 40; // XTERM_COLOR_40); //0xFF00D700);
	/** xterm color code. */
	private static final int CODE_41 = 41; // XTERM_COLOR_41); //0xFF00D75F);
	/** xterm color code. */
	private static final int CODE_42 = 42; // XTERM_COLOR_42); //0xFF00D787);
	/** xterm color code. */
	private static final int CODE_43 = 43; // XTERM_COLOR_43); //0xFF00D7AF);
	/** xterm color code. */
	private static final int CODE_44 = 44; // XTERM_COLOR_44); //0xFF00D7D7);
	/** xterm color code. */
	private static final int CODE_45 = 45; // XTERM_COLOR_45); //0xFF00D7FF);
	/** xterm color code. */
	private static final int CODE_46 = 46; // XTERM_COLOR_46); //0xFF00FF00);
	/** xterm color code. */
	private static final int CODE_47 = 47; // XTERM_COLOR_47); //0xFF00FF5F);
	/** xterm color code. */
	private static final int CODE_48 = 48; // XTERM_COLOR_48); //0xFF00FF87);
	/** xterm color code. */
	private static final int CODE_49 = 49; // XTERM_COLOR_49); //0xFF00FFAF);
	/** xterm color code. */
	private static final int CODE_50 = 50; // XTERM_COLOR_50); //0xFF00FFD7);
	/** xterm color code. */
	private static final int CODE_51 = 51; // XTERM_COLOR_51); //0xFF00FFFF);
	/** xterm color code. */
	private static final int CODE_52 = 52; // XTERM_COLOR_52); //0xFF5F0000);
	/** xterm color code. */
	private static final int CODE_53 = 53; // XTERM_COLOR_53); //0xFF5F005F);
	/** xterm color code. */
	private static final int CODE_54 = 54; // XTERM_COLOR_54); //0xFF5F0087);
	/** xterm color code. */
	private static final int CODE_55 = 55; // XTERM_COLOR_55); //0xFF5F00AF);
	/** xterm color code. */
	private static final int CODE_56 = 56; // XTERM_COLOR_56); //0xFF5F00D7);
	/** xterm color code. */
	private static final int CODE_57 = 57; // XTERM_COLOR_57); //0xFF5F00FF);
	/** xterm color code. */
	private static final int CODE_58 = 58; // XTERM_COLOR_58); //0xFF5F5F00);
	/** xterm color code. */
	private static final int CODE_59 = 59; // XTERM_COLOR_59); //0xFF5F5F5F);
	/** xterm color code. */
	private static final int CODE_60 = 60; // XTERM_COLOR_60); //0xFF5F5F87);
	/** xterm color code. */
	private static final int CODE_61 = 61; // XTERM_COLOR_61); //0xFF5F5FAF);
	/** xterm color code. */
	private static final int CODE_62 = 62; // XTERM_COLOR_62); //0xFF5F5FD7);
	/** xterm color code. */
	private static final int CODE_63 = 63; // XTERM_COLOR_63); //0xFF5F5FFF);
	/** xterm color code. */
	private static final int CODE_64 = 64; // XTERM_COLOR_64); //0xFF5F8700);
	/** xterm color code. */
	private static final int CODE_65 = 65; // XTERM_COLOR_65); //0xFF5F875F);
	/** xterm color code. */
	private static final int CODE_66 = 66; // XTERM_COLOR_66); //0xFF5F8787);
	/** xterm color code. */
	private static final int CODE_67 = 67; // XTERM_COLOR_67); //0xFF5F87AF);
	/** xterm color code. */
	private static final int CODE_68 = 68; // XTERM_COLOR_68); //0xFF5F87D7);
	/** xterm color code. */
	private static final int CODE_69 = 69; // XTERM_COLOR_69); //0xFF5F87FF);
	/** xterm color code. */
	private static final int CODE_70 = 70; // XTERM_COLOR_70); //0xFF5FAF00);
	/** xterm color code. */
	private static final int CODE_71 = 71; // XTERM_COLOR_71); //0xFF5FAF5F);
	/** xterm color code. */
	private static final int CODE_72 = 72; // XTERM_COLOR_72); //0xFF5FAF87);
	/** xterm color code. */
	private static final int CODE_73 = 73; // XTERM_COLOR_73); //0xFF5FAFAF);
	/** xterm color code. */
	private static final int CODE_74 = 74; // XTERM_COLOR_74); //0xFF5FAFD7);
	/** xterm color code. */
	private static final int CODE_75 = 75; // XTERM_COLOR_75); //0xFF5FAFFF);
	/** xterm color code. */
	private static final int CODE_76 = 76; // XTERM_COLOR_76); //0xFF5FD700);
	/** xterm color code. */
	private static final int CODE_77 = 77; // XTERM_COLOR_77); //0xFF5FD75F);
	/** xterm color code. */
	private static final int CODE_78 = 78; // XTERM_COLOR_78); //0xFF5FD787);
	/** xterm color code. */
	private static final int CODE_79 = 79; // XTERM_COLOR_79); //0xFF5FD7AF);
	/** xterm color code. */
	private static final int CODE_80 = 80; // XTERM_COLOR_80); //0xFF5FD7D7);
	/** xterm color code. */
	private static final int CODE_81 = 81; // XTERM_COLOR_81); //0xFF5FD7FF);
	/** xterm color code. */
	private static final int CODE_82 = 82; // XTERM_COLOR_82); //0xFF5FFF00);
	/** xterm color code. */
	private static final int CODE_83 = 83; // XTERM_COLOR_83); //0xFF5FFF5F);
	/** xterm color code. */
	private static final int CODE_84 = 84; // XTERM_COLOR_84); //0xFF5FFF87);
	/** xterm color code. */
	private static final int CODE_85 = 85; // XTERM_COLOR_85); //0xFF5FFFAF);
	/** xterm color code. */
	private static final int CODE_86 = 86; // XTERM_COLOR_86); //0xFF5FFFD7);
	/** xterm color code. */
	private static final int CODE_87 = 87; // XTERM_COLOR_87); //0xFF5FFFFF);
	/** xterm color code. */
	private static final int CODE_88 = 88; // XTERM_COLOR_88); //0xFF870000);
	/** xterm color code. */
	private static final int CODE_89 = 89; // XTERM_COLOR_89); //0xFF87005F);
	/** xterm color code. */
	private static final int CODE_90 = 90; // XTERM_COLOR_90); //0xFF870087);
	/** xterm color code. */
	private static final int CODE_91 = 91; // XTERM_COLOR_91); //0xFF8700AF);
	/** xterm color code. */
	private static final int CODE_92 = 92; // XTERM_COLOR_92); //0xFF8700D7);
	/** xterm color code. */
	private static final int CODE_93 = 93; // XTERM_COLOR_93); //0xFF8700FF);
	/** xterm color code. */
	private static final int CODE_94 = 94; // XTERM_COLOR_94); //0xFF875F00);
	/** xterm color code. */
	private static final int CODE_95 = 95; // XTERM_COLOR_95); //0xFF875F5F);
	/** xterm color code. */
	private static final int CODE_96 = 96; // XTERM_COLOR_96); //0xFF875F87);
	/** xterm color code. */
	private static final int CODE_97 = 97; // XTERM_COLOR_97); //0xFF875FAF);
	/** xterm color code. */
	private static final int CODE_98 = 98; // XTERM_COLOR_98); //0xFF875FD7);
	/** xterm color code. */
	private static final int CODE_99 = 99; // XTERM_COLOR_99); //0xFF875FFF);
	/** xterm color code. */
	private static final int CODE_100 = 100; // XTERM_COLOR_100); //0xFF878700);
	/** xterm color code. */
	private static final int CODE_101 = 101; // XTERM_COLOR_101); //0xFF87875F);
	/** xterm color code. */
	private static final int CODE_102 = 102; // XTERM_COLOR_102); //0xFF878787);
	/** xterm color code. */
	private static final int CODE_103 = 103; // XTERM_COLOR_103); //0xFF8787AF);
	/** xterm color code. */
	private static final int CODE_104 = 104; // XTERM_COLOR_104); //0xFF8787D7);
	/** xterm color code. */
	private static final int CODE_105 = 105; // XTERM_COLOR_105); //0xFF8787FF);
	/** xterm color code. */
	private static final int CODE_106 = 106; // XTERM_COLOR_106); //0xFF87AF00);
	/** xterm color code. */
	private static final int CODE_107 = 107; // XTERM_COLOR_107); //0xFF87AF5F);
	/** xterm color code. */
	private static final int CODE_108 = 108; // XTERM_COLOR_108); //0xFF87AF87);
	/** xterm color code. */
	private static final int CODE_109 = 109; // XTERM_COLOR_109); //0xFF87AFAF);
	/** xterm color code. */
	private static final int CODE_110 = 110; // XTERM_COLOR_110); //0xFF87AFD7);
	/** xterm color code. */
	private static final int CODE_111 = 111; // XTERM_COLOR_111); //0xFF87AFFF);
	/** xterm color code. */
	private static final int CODE_112 = 112; // XTERM_COLOR_112); //0xFF87D700);
	/** xterm color code. */
	private static final int CODE_113 = 113; // XTERM_COLOR_113); //0xFF87D75F);
	/** xterm color code. */
	private static final int CODE_114 = 114; // XTERM_COLOR_114); //0xFF87D787);
	/** xterm color code. */
	private static final int CODE_115 = 115; // XTERM_COLOR_115); //0xFF87D7AF);
	/** xterm color code. */
	private static final int CODE_116 = 116; // XTERM_COLOR_116); //0xFF87D7D7);
	/** xterm color code. */
	private static final int CODE_117 = 117; // XTERM_COLOR_117); //0xFF87D7FF);
	/** xterm color code. */
	private static final int CODE_118 = 118; // XTERM_COLOR_118); //0xFF87FF00);
	/** xterm color code. */
	private static final int CODE_119 = 119; // XTERM_COLOR_119); //0xFF87FF5F);
	/** xterm color code. */
	private static final int CODE_120 = 120; // XTERM_COLOR_120); //0xFF87FF87);
	/** xterm color code. */
	private static final int CODE_121 = 121; // XTERM_COLOR_121); //0xFF87FFAF);
	/** xterm color code. */
	private static final int CODE_122 = 122; // XTERM_COLOR_122); //0xFF87FFD7);
	/** xterm color code. */
	private static final int CODE_123 = 123; // XTERM_COLOR_123); //0xFF87FFFF);
	/** xterm color code. */
	private static final int CODE_124 = 124; // XTERM_COLOR_124); //0xFFAF0000);
	/** xterm color code. */
	private static final int CODE_125 = 125; // XTERM_COLOR_125); //0xFFAF005F);
	/** xterm color code. */
	private static final int CODE_126 = 126; // XTERM_COLOR_126); //0xFFAF0087);
	/** xterm color code. */
	private static final int CODE_127 = 127; // XTERM_COLOR_127); //0xFFAF00AF);
	/** xterm color code. */
	private static final int CODE_128 = 128; // XTERM_COLOR_128); //0xFFAF00D7);
	/** xterm color code. */
	private static final int CODE_129 = 129; // XTERM_COLOR_129); //0xFFAF00FF);
	/** xterm color code. */
	private static final int CODE_130 = 130; // XTERM_COLOR_130); //0xFFAF5F00);
	/** xterm color code. */
	private static final int CODE_131 = 131; // XTERM_COLOR_131); //0xFFAF5F5F);
	/** xterm color code. */
	private static final int CODE_132 = 132; // XTERM_COLOR_132); //0xFFAF5F87);
	/** xterm color code. */
	private static final int CODE_133 = 133; // XTERM_COLOR_133); //0xFFAF5FAF);
	/** xterm color code. */
	private static final int CODE_134 = 134; // XTERM_COLOR_134); //0xFFAF5FD7);
	/** xterm color code. */
	private static final int CODE_135 = 135; // XTERM_COLOR_135); //0xFFAF5FFF);
	/** xterm color code. */
	private static final int CODE_136 = 136; // XTERM_COLOR_136); //0xFFAF8700);
	/** xterm color code. */
	private static final int CODE_137 = 137; // XTERM_COLOR_137); //0xFFAF875F);
	/** xterm color code. */
	private static final int CODE_138 = 138; // XTERM_COLOR_138); //0xFFAF8787);
	/** xterm color code. */
	private static final int CODE_139 = 139; // XTERM_COLOR_139); //0xFFAF87AF);
	/** xterm color code. */
	private static final int CODE_140 = 140; // XTERM_COLOR_140); //0xFFAF87D7);
	/** xterm color code. */
	private static final int CODE_141 = 141; // XTERM_COLOR_141); //0xFFAF87FF);
	/** xterm color code. */
	private static final int CODE_142 = 142; // XTERM_COLOR_142); //0xFFAFAF00);
	/** xterm color code. */
	private static final int CODE_143 = 143; // XTERM_COLOR_143); //0xFFAFAF5F);
	/** xterm color code. */
	private static final int CODE_144 = 144; // XTERM_COLOR_144); //0xFFAFAF87);
	/** xterm color code. */
	private static final int CODE_145 = 145; // XTERM_COLOR_145); //0xFFAFAFAF);
	/** xterm color code. */
	private static final int CODE_146 = 146; // XTERM_COLOR_146); //0xFFAFAFD7);
	/** xterm color code. */
	private static final int CODE_147 = 147; // XTERM_COLOR_147); //0xFFAFAFFF);
	/** xterm color code. */
	private static final int CODE_148 = 148; // XTERM_COLOR_148); //0xFFAFD700);
	/** xterm color code. */
	private static final int CODE_149 = 149; // XTERM_COLOR_149); //0xFFAFD75F);
	/** xterm color code. */
	private static final int CODE_150 = 150; // XTERM_COLOR_150); //0xFFAFD787);
	/** xterm color code. */
	private static final int CODE_151 = 151; // XTERM_COLOR_151); //0xFFAFD7AF);
	/** xterm color code. */
	private static final int CODE_152 = 152; // XTERM_COLOR_152); //0xFFAFD7D7);
	/** xterm color code. */
	private static final int CODE_153 = 153; // XTERM_COLOR_153); //0xFFAFD7FF);
	/** xterm color code. */
	private static final int CODE_154 = 154; // XTERM_COLOR_154); //0xFFAFFF00);
	/** xterm color code. */
	private static final int CODE_155 = 155; // XTERM_COLOR_155); //0xFFAFFF5F);
	/** xterm color code. */
	private static final int CODE_156 = 156; // XTERM_COLOR_156); //0xFFAFFF87);
	/** xterm color code. */
	private static final int CODE_157 = 157; // XTERM_COLOR_157); //0xFFAFFFAF);
	/** xterm color code. */
	private static final int CODE_158 = 158; // XTERM_COLOR_158); //0xFFAFFFD7);
	/** xterm color code. */
	private static final int CODE_159 = 159; // XTERM_COLOR_159); //0xFFAFFFFF);
	/** xterm color code. */
	private static final int CODE_160 = 160; // XTERM_COLOR_160); //0xFFD70000);
	/** xterm color code. */
	private static final int CODE_161 = 161; // XTERM_COLOR_161); //0xFFD7005F);
	/** xterm color code. */
	private static final int CODE_162 = 162; // XTERM_COLOR_162); //0xFFD70087);
	/** xterm color code. */
	private static final int CODE_163 = 163; // XTERM_COLOR_163); //0xFFD700AF);
	/** xterm color code. */
	private static final int CODE_164 = 164; // XTERM_COLOR_164); //0xFFD700D7);
	/** xterm color code. */
	private static final int CODE_165 = 165; // XTERM_COLOR_165); //0xFFD700FF);
	/** xterm color code. */
	private static final int CODE_166 = 166; // XTERM_COLOR_166); //0xFFD75F00);
	/** xterm color code. */
	private static final int CODE_167 = 167; // XTERM_COLOR_167); //0xFFD75F5F);
	/** xterm color code. */
	private static final int CODE_168 = 168; // XTERM_COLOR_168); //0xFFD75F87);
	/** xterm color code. */
	private static final int CODE_169 = 169; // XTERM_COLOR_169); //0xFFD75FAF);
	/** xterm color code. */
	private static final int CODE_170 = 170; // XTERM_COLOR_170); //0xFFD75FD7);
	/** xterm color code. */
	private static final int CODE_171 = 171; // XTERM_COLOR_171); //0xFFD75FFF);
	/** xterm color code. */
	private static final int CODE_172 = 172; // XTERM_COLOR_172); //0xFFD78700);
	/** xterm color code. */
	private static final int CODE_173 = 173; // XTERM_COLOR_173); //0xFFD7875F);
	/** xterm color code. */
	private static final int CODE_174 = 174; // XTERM_COLOR_174); //0xFFD78787);
	/** xterm color code. */
	private static final int CODE_175 = 175; // XTERM_COLOR_175); //0xFFD787AF);
	/** xterm color code. */
	private static final int CODE_176 = 176; // XTERM_COLOR_176); //0xFFD787D7);
	/** xterm color code. */
	private static final int CODE_177 = 177; // XTERM_COLOR_177); //0xFFD787FF);
	/** xterm color code. */
	private static final int CODE_178 = 178; // XTERM_COLOR_178); //0xFFD7AF00);
	/** xterm color code. */
	private static final int CODE_179 = 179; // XTERM_COLOR_179); //0xFFD7AF5F);
	/** xterm color code. */
	private static final int CODE_180 = 180; // XTERM_COLOR_180); //0xFFD7AF87);
	/** xterm color code. */
	private static final int CODE_181 = 181; // XTERM_COLOR_181); //0xFFD7AFAF);
	/** xterm color code. */
	private static final int CODE_182 = 182; // XTERM_COLOR_182); //0xFFD7AFD7);
	/** xterm color code. */
	private static final int CODE_183 = 183; // XTERM_COLOR_183); //0xFFD7AFFF);
	/** xterm color code. */
	private static final int CODE_184 = 184; // XTERM_COLOR_184); //0xFFD7D700);
	/** xterm color code. */
	private static final int CODE_185 = 185; // XTERM_COLOR_185); //0xFFD7D75F);
	/** xterm color code. */
	private static final int CODE_186 = 186; // XTERM_COLOR_186); //0xFFD7D787);
	/** xterm color code. */
	private static final int CODE_187 = 187; // XTERM_COLOR_187); //0xFFD7D7AF);
	/** xterm color code. */
	private static final int CODE_188 = 188; // XTERM_COLOR_188); //0xFFD7D7D7);
	/** xterm color code. */
	private static final int CODE_189 = 189; // XTERM_COLOR_189); //0xFFD7D7FF);
	/** xterm color code. */
	private static final int CODE_190 = 190; // XTERM_COLOR_190); //0xFFD7FF00);
	/** xterm color code. */
	private static final int CODE_191 = 191; // XTERM_COLOR_191); //0xFFD7FF5F);
	/** xterm color code. */
	private static final int CODE_192 = 192; // XTERM_COLOR_192); //0xFFD7FF87);
	/** xterm color code. */
	private static final int CODE_193 = 193; // XTERM_COLOR_193); //0xFFD7FFAF);
	/** xterm color code. */
	private static final int CODE_194 = 194; // XTERM_COLOR_194); //0xFFD7FFD7);
	/** xterm color code. */
	private static final int CODE_195 = 195; // XTERM_COLOR_195); //0xFFD7FFFF);
	/** xterm color code. */
	private static final int CODE_196 = 196; // XTERM_COLOR_196); //0xFFFF0000);
	/** xterm color code. */
	private static final int CODE_197 = 197; // XTERM_COLOR_197); //0xFFFF005F);
	/** xterm color code. */
	private static final int CODE_198 = 198; // XTERM_COLOR_198); //0xFFFF0087);
	/** xterm color code. */
	private static final int CODE_199 = 199; // XTERM_COLOR_199); //0xFFFF00AF);
	/** xterm color code. */
	private static final int CODE_200 = 200; // XTERM_COLOR_200); //0xFFFF00D7);
	/** xterm color code. */
	private static final int CODE_201 = 201; // XTERM_COLOR_201); //0xFFFF00FF);
	/** xterm color code. */
	private static final int CODE_202 = 202; // XTERM_COLOR_202); //0xFFFF5F00);
	/** xterm color code. */
	private static final int CODE_203 = 203; // XTERM_COLOR_203); //0xFFFF5F5F);
	/** xterm color code. */
	private static final int CODE_204 = 204; // XTERM_COLOR_204); //0xFFFF5F87);
	/** xterm color code. */
	private static final int CODE_205 = 205; // XTERM_COLOR_205); //0xFFFF5FAF);
	/** xterm color code. */
	private static final int CODE_206 = 206; // XTERM_COLOR_206); //0xFFFF5FD7);
	/** xterm color code. */
	private static final int CODE_207 = 207; // XTERM_COLOR_207); //0xFFFF5FFF);
	/** xterm color code. */
	private static final int CODE_208 = 208; // XTERM_COLOR_208); //0xFFFF8700);
	/** xterm color code. */
	private static final int CODE_209 = 209; // XTERM_COLOR_209); //0xFFFF875F);
	/** xterm color code. */
	private static final int CODE_210 = 210; // XTERM_COLOR_210); //0xFFFF8787);
	/** xterm color code. */
	private static final int CODE_211 = 211; // XTERM_COLOR_211); //0xFFFF87AF);
	/** xterm color code. */
	private static final int CODE_212 = 212; // XTERM_COLOR_212); //0xFFFF87D7);
	/** xterm color code. */
	private static final int CODE_213 = 213; // XTERM_COLOR_213); //0xFFFF87FF);
	/** xterm color code. */
	private static final int CODE_214 = 214; // XTERM_COLOR_214); //0xFFFFAF00);
	/** xterm color code. */
	private static final int CODE_215 = 215; // XTERM_COLOR_215); //0xFFFFAF5F);
	/** xterm color code. */
	private static final int CODE_216 = 216; // XTERM_COLOR_216); //0xFFFFAF87);
	/** xterm color code. */
	private static final int CODE_217 = 217; // XTERM_COLOR_217); //0xFFFFAFAF);
	/** xterm color code. */
	private static final int CODE_218 = 218; // XTERM_COLOR_218); //0xFFFFAFD7);
	/** xterm color code. */
	private static final int CODE_219 = 219; // XTERM_COLOR_219); //0xFFFFAFFF);
	/** xterm color code. */
	private static final int CODE_220 = 220; // XTERM_COLOR_220); //0xFFFFD700);
	/** xterm color code. */
	private static final int CODE_221 = 221; // XTERM_COLOR_221); //0xFFFFD75F);
	/** xterm color code. */
	private static final int CODE_222 = 222; // XTERM_COLOR_222); //0xFFFFD787);
	/** xterm color code. */
	private static final int CODE_223 = 223; // XTERM_COLOR_223); //0xFFFFD7AF);
	/** xterm color code. */
	private static final int CODE_224 = 224; // XTERM_COLOR_224); //0xFFFFD7D7);
	/** xterm color code. */
	private static final int CODE_225 = 225; // XTERM_COLOR_225); //0xFFFFD7FF);
	/** xterm color code. */
	private static final int CODE_226 = 226; // XTERM_COLOR_226); //0xFFFFFF00);
	/** xterm color code. */
	private static final int CODE_227 = 227; // XTERM_COLOR_227); //0xFFFFFF5F);
	/** xterm color code. */
	private static final int CODE_228 = 228; // XTERM_COLOR_228); //0xFFFFFF87);
	/** xterm color code. */
	private static final int CODE_229 = 229; // XTERM_COLOR_229); //0xFFFFFFAF);
	/** xterm color code. */
	private static final int CODE_230 = 230; // XTERM_COLOR_230); //0xFFFFFFD7);
	/** xterm color code. */
	private static final int CODE_231 = 231; // XTERM_COLOR_231); //0xFFFFFFFF);
	/** xterm color code. */
	//blacks/grey
	/** xterm color code. */
	private static final int CODE_232 = 232; // XTERM_COLOR_232); //0xFF080808);
	/** xterm color code. */
	private static final int CODE_233 = 233; // XTERM_COLOR_233); //0xFF121212);
	/** xterm color code. */
	private static final int CODE_234 = 234; // XTERM_COLOR_234); //0xFF1C1C1C);
	/** xterm color code. */
	private static final int CODE_235 = 235; // XTERM_COLOR_235); //0xFF262626);
	/** xterm color code. */
	private static final int CODE_236 = 236; // XTERM_COLOR_236); //0xFF303030);
	/** xterm color code. */
	private static final int CODE_237 = 237; // XTERM_COLOR_237); //0xFF3A3A3A);
	/** xterm color code. */
	private static final int CODE_238 = 238; // XTERM_COLOR_238); //0xFF444444);
	/** xterm color code. */
	private static final int CODE_239 = 239; // XTERM_COLOR_239); //0xFF4E4E4E);
	/** xterm color code. */
	private static final int CODE_240 = 240; // XTERM_COLOR_240); //0xFF585858);
	/** xterm color code. */
	private static final int CODE_241 = 241; // XTERM_COLOR_241); //0xFF626262);
	/** xterm color code. */
	private static final int CODE_242 = 242; // XTERM_COLOR_242); //0xFF6C6C6C);
	/** xterm color code. */
	private static final int CODE_243 = 243; // XTERM_COLOR_243); //0xFF767676);
	/** xterm color code. */
	private static final int CODE_244 = 244; // XTERM_COLOR_244); //0xFF808080);
	/** xterm color code. */
	private static final int CODE_245 = 245; // XTERM_COLOR_245); //0xFF8A8A8A);
	/** xterm color code. */
	private static final int CODE_246 = 246; // XTERM_COLOR_246); //0xFF949494);
	/** xterm color code. */
	private static final int CODE_247 = 247; // XTERM_COLOR_247); //0xFF9E9E9E);
	/** xterm color code. */
	private static final int CODE_248 = 248; // XTERM_COLOR_248); //0xFFA8A8A8);
	/** xterm color code. */
	private static final int CODE_249 = 249; // XTERM_COLOR_249); //0xFFB2B2B2);
	/** xterm color code. */
	private static final int CODE_250 = 250; // XTERM_COLOR_250); //0xFFBCBCBC);
	/** xterm color code. */
	private static final int CODE_251 = 251; // XTERM_COLOR_251); //0xFFC6C6C6);
	/** xterm color code. */
	private static final int CODE_252 = 252; // XTERM_COLOR_252); //0xFFD0D0D0);
	/** xterm color code. */
	private static final int CODE_253 = 253; // XTERM_COLOR_253); //0xFFDADADA);
	/** xterm color code. */
	private static final int CODE_254 = 254; // XTERM_COLOR_254); //0xFFE4E4E4);
	/** xterm color code. */
	private static final int CODE_255 = 255; // XTERM_COLOR_255); //0xFFEEEEEE);
	
	/** ANSI black code. */
	private static final int ANSI_BLACK_CODE = 0;
	/** ANSI red code. */
	private static final int ANSI_RED_CODE = 1;
	/** ANSI green code. */
	private static final int ANSI_GREEN_CODE = 2;
	/** ANSI yellow code. */
	private static final int ANSI_YELLOW_CODE = 3;
	/** ANSI blue code. */
	private static final int ANSI_BLUE_CODE = 4;
	/** ANSI magenta code. */
	private static final int ANSI_MAGENTA_CODE = 5;
	/** ANSI cyan code. */
	private static final int ANSI_CYAN_CODE = 6;
	/** ANSI white code. */
	private static final int ANSI_WHITE_CODE = 7;
	
	/** ANSI red. */
	private static final int ANSI_RED = 0xFFBB0000;
	/** ANSI green. */
	private static final int ANSI_GREEN = 0xFF00BB00;
	/** ANSI yellow. */
	private static final int ANSI_YELLOW = 0xFFBBBB00;
	/** ANSI blue. */
	private static final int ANSI_BLUE = 0xFF0000EE;
	/** ANSI magenta. */
	private static final int ANSI_MAGENTA = 0xFFBB00BB;
	/** ANSI cyan. */
	private static final int ANSI_CYAN = 0xFF00BBBB;
	/** ANSI white. */
	private static final int ANSI_WHITE = 0xFFBBBBBB;
	
	/** ANSI bright black. */
	private static final int ANSI_BRIGHT_BLACK = 0xFF555555;
	/** ANSI bright red. */
	private static final int ANSI_BRIGHT_RED = 0xFFFF5555;
	/** ANSI bright green. */
	private static final int ANSI_BRIGHT_GREEN = 0xFF55FF55;
	/** ANSI bright yellow. */
	private static final int ANSI_BRIGHT_YELLOW = 0xFFFFFF55;
	/** ANSI bright blue. */
	private static final int ANSI_BRIGHT_BLUE = 0xFF5555FF;
	/** ANSI bright magenta. */
	private static final int ANSI_BRIGHT_MAGENTA = 0xFFFF55FF;
	/** ANSI bright cyan. */
	private static final int ANSI_BRIGHT_CYAN = 0xFF55FFFF;
	/** ANSI bright white. */
	private static final int ANSI_BRIGHT_WHITE = 0xFFFFFFFF;
	
	/** ANSI foreground. */
	private static final int ANSI_FOREGROUND_START = 30;
	/** ANSI background. */
	private static final int ANSI_BACKGROUND_START = 40;
	/** ANSI foreground 'ten spot' digit. */
	private static final int ANSI_FOREGROUND_TENSPOT = 3;
	/** ANSI background. */
	private static final int ANSI_BACKGROUND_TENSPOT = 4;
	/** ANSI max background color. */
	private static final int ANSI_BACKGROUND_MAX = 50;
	/** ANSI Default foreground. */
	private static final int ANSI_FOREGROUND_DEFAULT = 39;
	/** ANSI Default background. */
	private static final int ANSI_BACKGROUND_DEFAULT = 49;
	/** XTERM 256 foreground color code starting marker. */
	private static final int XTERM_256_FOREGROUND = 38;
	/** XTERM 256 background color code starting marker. */
	private static final int XTERM_256_BACKGROUND = 48;
	/** XTERM 256 five byte marker. */
	private static final int XTERM_256_FIVE = 5;
	/** The map of xterm 256 color codes and corresponding colors. */
	private static SparseIntArray colormap256 = new SparseIntArray() {

		{
			this.put(CODE_0, XTERM_COLOR_0); //0xFF000000); //BLACK
			this.put(CODE_1, XTERM_COLOR_1); //0xFFBB0000); //RED
			this.put(CODE_2, XTERM_COLOR_2); //0xFF00BB00); //GREEN
			this.put(CODE_3, XTERM_COLOR_3); //0xFFBBBB00); //YELLOW
			this.put(CODE_4, XTERM_COLOR_4); //0xFF0000EE); //BLUE
			this.put(CODE_5, XTERM_COLOR_5); //0xFFBB00BB); //MAGENTA
			this.put(CODE_6, XTERM_COLOR_6); //0xFF00BBBB); //CYAN
			this.put(CODE_7, XTERM_COLOR_7); //0xFFBBBBBB); //WHITE
			this.put(CODE_8, XTERM_COLOR_8); //0xFF555555); //BRIGHT BLACK (GREY)
			this.put(CODE_9, XTERM_COLOR_9); //0xFFFF5555); //BRIGHT RED
			this.put(CODE_10, XTERM_COLOR_10); //0xFF55FF55); //BRIGHT GREEN
			this.put(CODE_11, XTERM_COLOR_11); //0xFFFFFF55); //BRIGHT YELLOW
			this.put(CODE_12, XTERM_COLOR_12); //0xFF5555FF); //BRIGHT BLUE
			this.put(CODE_13, XTERM_COLOR_13); //0xFFFF55FF); //BRIGHT MAGENTA
			this.put(CODE_14, XTERM_COLOR_14); //0xFF55FFFF); //BRIGHT CYAN
			this.put(CODE_15, XTERM_COLOR_15); //0xFFFFFFFF); //BRIGHT WHITE
			
			//start normative xterm256 color
			this.put(CODE_16, XTERM_COLOR_16); //0xFF000000);
			this.put(CODE_17, XTERM_COLOR_17); //0xFF00005F);
			this.put(CODE_18, XTERM_COLOR_18); //0xFF000087);
			this.put(CODE_19, XTERM_COLOR_19); //0xFF0000AF);
			this.put(CODE_20, XTERM_COLOR_20); //0xFF0000D7);
			this.put(CODE_21, XTERM_COLOR_21); //0xFF0000FF);
			this.put(CODE_22, XTERM_COLOR_22); //0xFF005F00);
			this.put(CODE_23, XTERM_COLOR_23); //0xFF005F5F);
			this.put(CODE_24, XTERM_COLOR_24); //0xFF005F87);
			this.put(CODE_25, XTERM_COLOR_25); //0xFF005FAF);
			this.put(CODE_26, XTERM_COLOR_26); //0xFF005FD7);
			this.put(CODE_27, XTERM_COLOR_27); //0xFF005FFF);
			this.put(CODE_28, XTERM_COLOR_28); //0xFF008700);
			this.put(CODE_29, XTERM_COLOR_29); //0xFF00875F);
			this.put(CODE_30, XTERM_COLOR_30); //0xFF008787);
			this.put(CODE_31, XTERM_COLOR_31); //0xFF0087AF);
			this.put(CODE_32, XTERM_COLOR_32); //0xFF0087D7);
			this.put(CODE_33, XTERM_COLOR_33); //0xFF0087FF);
			this.put(CODE_34, XTERM_COLOR_34); //0xFF00AF00);
			this.put(CODE_35, XTERM_COLOR_35); //0xFF00AF5F);
			this.put(CODE_36, XTERM_COLOR_36); //0xFF00AF87);
			this.put(CODE_37, XTERM_COLOR_37); //0xFF00AFAF);
			this.put(CODE_38, XTERM_COLOR_38); //0xFF00AFD7);
			this.put(CODE_39, XTERM_COLOR_39); //0xFF00AFFF);
			this.put(CODE_40, XTERM_COLOR_40); //0xFF00D700);
			this.put(CODE_41, XTERM_COLOR_41); //0xFF00D75F);
			this.put(CODE_42, XTERM_COLOR_42); //0xFF00D787);
			this.put(CODE_43, XTERM_COLOR_43); //0xFF00D7AF);
			this.put(CODE_44, XTERM_COLOR_44); //0xFF00D7D7);
			this.put(CODE_45, XTERM_COLOR_45); //0xFF00D7FF);
			this.put(CODE_46, XTERM_COLOR_46); //0xFF00FF00);
			this.put(CODE_47, XTERM_COLOR_47); //0xFF00FF5F);
			this.put(CODE_48, XTERM_COLOR_48); //0xFF00FF87);
			this.put(CODE_49, XTERM_COLOR_49); //0xFF00FFAF);
			this.put(CODE_50, XTERM_COLOR_50); //0xFF00FFD7);
			this.put(CODE_51, XTERM_COLOR_51); //0xFF00FFFF);
			this.put(CODE_52, XTERM_COLOR_52); //0xFF5F0000);
			this.put(CODE_53, XTERM_COLOR_53); //0xFF5F005F);
			this.put(CODE_54, XTERM_COLOR_54); //0xFF5F0087);
			this.put(CODE_55, XTERM_COLOR_55); //0xFF5F00AF);
			this.put(CODE_56, XTERM_COLOR_56); //0xFF5F00D7);
			this.put(CODE_57, XTERM_COLOR_57); //0xFF5F00FF);
			this.put(CODE_58, XTERM_COLOR_58); //0xFF5F5F00);
			this.put(CODE_59, XTERM_COLOR_59); //0xFF5F5F5F);
			this.put(CODE_60, XTERM_COLOR_60); //0xFF5F5F87);
			this.put(CODE_61, XTERM_COLOR_61); //0xFF5F5FAF);
			this.put(CODE_62, XTERM_COLOR_62); //0xFF5F5FD7);
			this.put(CODE_63, XTERM_COLOR_63); //0xFF5F5FFF);
			this.put(CODE_64, XTERM_COLOR_64); //0xFF5F8700);
			this.put(CODE_65, XTERM_COLOR_65); //0xFF5F875F);
			this.put(CODE_66, XTERM_COLOR_66); //0xFF5F8787);
			this.put(CODE_67, XTERM_COLOR_67); //0xFF5F87AF);
			this.put(CODE_68, XTERM_COLOR_68); //0xFF5F87D7);
			this.put(CODE_69, XTERM_COLOR_69); //0xFF5F87FF);
			this.put(CODE_70, XTERM_COLOR_70); //0xFF5FAF00);
			this.put(CODE_71, XTERM_COLOR_71); //0xFF5FAF5F);
			this.put(CODE_72, XTERM_COLOR_72); //0xFF5FAF87);
			this.put(CODE_73, XTERM_COLOR_73); //0xFF5FAFAF);
			this.put(CODE_74, XTERM_COLOR_74); //0xFF5FAFD7);
			this.put(CODE_75, XTERM_COLOR_75); //0xFF5FAFFF);
			this.put(CODE_76, XTERM_COLOR_76); //0xFF5FD700);
			this.put(CODE_77, XTERM_COLOR_77); //0xFF5FD75F);
			this.put(CODE_78, XTERM_COLOR_78); //0xFF5FD787);
			this.put(CODE_79, XTERM_COLOR_79); //0xFF5FD7AF);
			this.put(CODE_80, XTERM_COLOR_80); //0xFF5FD7D7);
			this.put(CODE_81, XTERM_COLOR_81); //0xFF5FD7FF);
			this.put(CODE_82, XTERM_COLOR_82); //0xFF5FFF00);
			this.put(CODE_83, XTERM_COLOR_83); //0xFF5FFF5F);
			this.put(CODE_84, XTERM_COLOR_84); //0xFF5FFF87);
			this.put(CODE_85, XTERM_COLOR_85); //0xFF5FFFAF);
			this.put(CODE_86, XTERM_COLOR_86); //0xFF5FFFD7);
			this.put(CODE_87, XTERM_COLOR_87); //0xFF5FFFFF);
			this.put(CODE_88, XTERM_COLOR_88); //0xFF870000);
			this.put(CODE_89, XTERM_COLOR_89); //0xFF87005F);
			this.put(CODE_90, XTERM_COLOR_90); //0xFF870087);
			this.put(CODE_91, XTERM_COLOR_91); //0xFF8700AF);
			this.put(CODE_92, XTERM_COLOR_92); //0xFF8700D7);
			this.put(CODE_93, XTERM_COLOR_93); //0xFF8700FF);
			this.put(CODE_94, XTERM_COLOR_94); //0xFF875F00);
			this.put(CODE_95, XTERM_COLOR_95); //0xFF875F5F);
			this.put(CODE_96, XTERM_COLOR_96); //0xFF875F87);
			this.put(CODE_97, XTERM_COLOR_97); //0xFF875FAF);
			this.put(CODE_98, XTERM_COLOR_98); //0xFF875FD7);
			this.put(CODE_99, XTERM_COLOR_99); //0xFF875FFF);
			this.put(CODE_100, XTERM_COLOR_100); //0xFF878700);
			this.put(CODE_101, XTERM_COLOR_101); //0xFF87875F);
			this.put(CODE_102, XTERM_COLOR_102); //0xFF878787);
			this.put(CODE_103, XTERM_COLOR_103); //0xFF8787AF);
			this.put(CODE_104, XTERM_COLOR_104); //0xFF8787D7);
			this.put(CODE_105, XTERM_COLOR_105); //0xFF8787FF);
			this.put(CODE_106, XTERM_COLOR_106); //0xFF87AF00);
			this.put(CODE_107, XTERM_COLOR_107); //0xFF87AF5F);
			this.put(CODE_108, XTERM_COLOR_108); //0xFF87AF87);
			this.put(CODE_109, XTERM_COLOR_109); //0xFF87AFAF);
			this.put(CODE_110, XTERM_COLOR_110); //0xFF87AFD7);
			this.put(CODE_111, XTERM_COLOR_111); //0xFF87AFFF);
			this.put(CODE_112, XTERM_COLOR_112); //0xFF87D700);
			this.put(CODE_113, XTERM_COLOR_113); //0xFF87D75F);
			this.put(CODE_114, XTERM_COLOR_114); //0xFF87D787);
			this.put(CODE_115, XTERM_COLOR_115); //0xFF87D7AF);
			this.put(CODE_116, XTERM_COLOR_116); //0xFF87D7D7);
			this.put(CODE_117, XTERM_COLOR_117); //0xFF87D7FF);
			this.put(CODE_118, XTERM_COLOR_118); //0xFF87FF00);
			this.put(CODE_119, XTERM_COLOR_119); //0xFF87FF5F);
			this.put(CODE_120, XTERM_COLOR_120); //0xFF87FF87);
			this.put(CODE_121, XTERM_COLOR_121); //0xFF87FFAF);
			this.put(CODE_122, XTERM_COLOR_122); //0xFF87FFD7);
			this.put(CODE_123, XTERM_COLOR_123); //0xFF87FFFF);
			this.put(CODE_124, XTERM_COLOR_124); //0xFFAF0000);
			this.put(CODE_125, XTERM_COLOR_125); //0xFFAF005F);
			this.put(CODE_126, XTERM_COLOR_126); //0xFFAF0087);
			this.put(CODE_127, XTERM_COLOR_127); //0xFFAF00AF);
			this.put(CODE_128, XTERM_COLOR_128); //0xFFAF00D7);
			this.put(CODE_129, XTERM_COLOR_129); //0xFFAF00FF);
			this.put(CODE_130, XTERM_COLOR_130); //0xFFAF5F00);
			this.put(CODE_131, XTERM_COLOR_131); //0xFFAF5F5F);
			this.put(CODE_132, XTERM_COLOR_132); //0xFFAF5F87);
			this.put(CODE_133, XTERM_COLOR_133); //0xFFAF5FAF);
			this.put(CODE_134, XTERM_COLOR_134); //0xFFAF5FD7);
			this.put(CODE_135, XTERM_COLOR_135); //0xFFAF5FFF);
			this.put(CODE_136, XTERM_COLOR_136); //0xFFAF8700);
			this.put(CODE_137, XTERM_COLOR_137); //0xFFAF875F);
			this.put(CODE_138, XTERM_COLOR_138); //0xFFAF8787);
			this.put(CODE_139, XTERM_COLOR_139); //0xFFAF87AF);
			this.put(CODE_140, XTERM_COLOR_140); //0xFFAF87D7);
			this.put(CODE_141, XTERM_COLOR_141); //0xFFAF87FF);
			this.put(CODE_142, XTERM_COLOR_142); //0xFFAFAF00);
			this.put(CODE_143, XTERM_COLOR_143); //0xFFAFAF5F);
			this.put(CODE_144, XTERM_COLOR_144); //0xFFAFAF87);
			this.put(CODE_145, XTERM_COLOR_145); //0xFFAFAFAF);
			this.put(CODE_146, XTERM_COLOR_146); //0xFFAFAFD7);
			this.put(CODE_147, XTERM_COLOR_147); //0xFFAFAFFF);
			this.put(CODE_148, XTERM_COLOR_148); //0xFFAFD700);
			this.put(CODE_149, XTERM_COLOR_149); //0xFFAFD75F);
			this.put(CODE_150, XTERM_COLOR_150); //0xFFAFD787);
			this.put(CODE_151, XTERM_COLOR_151); //0xFFAFD7AF);
			this.put(CODE_152, XTERM_COLOR_152); //0xFFAFD7D7);
			this.put(CODE_153, XTERM_COLOR_153); //0xFFAFD7FF);
			this.put(CODE_154, XTERM_COLOR_154); //0xFFAFFF00);
			this.put(CODE_155, XTERM_COLOR_155); //0xFFAFFF5F);
			this.put(CODE_156, XTERM_COLOR_156); //0xFFAFFF87);
			this.put(CODE_157, XTERM_COLOR_157); //0xFFAFFFAF);
			this.put(CODE_158, XTERM_COLOR_158); //0xFFAFFFD7);
			this.put(CODE_159, XTERM_COLOR_159); //0xFFAFFFFF);
			this.put(CODE_160, XTERM_COLOR_160); //0xFFD70000);
			this.put(CODE_161, XTERM_COLOR_161); //0xFFD7005F);
			this.put(CODE_162, XTERM_COLOR_162); //0xFFD70087);
			this.put(CODE_163, XTERM_COLOR_163); //0xFFD700AF);
			this.put(CODE_164, XTERM_COLOR_164); //0xFFD700D7);
			this.put(CODE_165, XTERM_COLOR_165); //0xFFD700FF);
			this.put(CODE_166, XTERM_COLOR_166); //0xFFD75F00);
			this.put(CODE_167, XTERM_COLOR_167); //0xFFD75F5F);
			this.put(CODE_168, XTERM_COLOR_168); //0xFFD75F87);
			this.put(CODE_169, XTERM_COLOR_169); //0xFFD75FAF);
			this.put(CODE_170, XTERM_COLOR_170); //0xFFD75FD7);
			this.put(CODE_171, XTERM_COLOR_171); //0xFFD75FFF);
			this.put(CODE_172, XTERM_COLOR_172); //0xFFD78700);
			this.put(CODE_173, XTERM_COLOR_173); //0xFFD7875F);
			this.put(CODE_174, XTERM_COLOR_174); //0xFFD78787);
			this.put(CODE_175, XTERM_COLOR_175); //0xFFD787AF);
			this.put(CODE_176, XTERM_COLOR_176); //0xFFD787D7);
			this.put(CODE_177, XTERM_COLOR_177); //0xFFD787FF);
			this.put(CODE_178, XTERM_COLOR_178); //0xFFD7AF00);
			this.put(CODE_179, XTERM_COLOR_179); //0xFFD7AF5F);
			this.put(CODE_180, XTERM_COLOR_180); //0xFFD7AF87);
			this.put(CODE_181, XTERM_COLOR_181); //0xFFD7AFAF);
			this.put(CODE_182, XTERM_COLOR_182); //0xFFD7AFD7);
			this.put(CODE_183, XTERM_COLOR_183); //0xFFD7AFFF);
			this.put(CODE_184, XTERM_COLOR_184); //0xFFD7D700);
			this.put(CODE_185, XTERM_COLOR_185); //0xFFD7D75F);
			this.put(CODE_186, XTERM_COLOR_186); //0xFFD7D787);
			this.put(CODE_187, XTERM_COLOR_187); //0xFFD7D7AF);
			this.put(CODE_188, XTERM_COLOR_188); //0xFFD7D7D7);
			this.put(CODE_189, XTERM_COLOR_189); //0xFFD7D7FF);
			this.put(CODE_190, XTERM_COLOR_190); //0xFFD7FF00);
			this.put(CODE_191, XTERM_COLOR_191); //0xFFD7FF5F);
			this.put(CODE_192, XTERM_COLOR_192); //0xFFD7FF87);
			this.put(CODE_193, XTERM_COLOR_193); //0xFFD7FFAF);
			this.put(CODE_194, XTERM_COLOR_194); //0xFFD7FFD7);
			this.put(CODE_195, XTERM_COLOR_195); //0xFFD7FFFF);
			this.put(CODE_196, XTERM_COLOR_196); //0xFFFF0000);
			this.put(CODE_197, XTERM_COLOR_197); //0xFFFF005F);
			this.put(CODE_198, XTERM_COLOR_198); //0xFFFF0087);
			this.put(CODE_199, XTERM_COLOR_199); //0xFFFF00AF);
			this.put(CODE_200, XTERM_COLOR_200); //0xFFFF00D7);
			this.put(CODE_201, XTERM_COLOR_201); //0xFFFF00FF);
			this.put(CODE_202, XTERM_COLOR_202); //0xFFFF5F00);
			this.put(CODE_203, XTERM_COLOR_203); //0xFFFF5F5F);
			this.put(CODE_204, XTERM_COLOR_204); //0xFFFF5F87);
			this.put(CODE_205, XTERM_COLOR_205); //0xFFFF5FAF);
			this.put(CODE_206, XTERM_COLOR_206); //0xFFFF5FD7);
			this.put(CODE_207, XTERM_COLOR_207); //0xFFFF5FFF);
			this.put(CODE_208, XTERM_COLOR_208); //0xFFFF8700);
			this.put(CODE_209, XTERM_COLOR_209); //0xFFFF875F);
			this.put(CODE_210, XTERM_COLOR_210); //0xFFFF8787);
			this.put(CODE_211, XTERM_COLOR_211); //0xFFFF87AF);
			this.put(CODE_212, XTERM_COLOR_212); //0xFFFF87D7);
			this.put(CODE_213, XTERM_COLOR_213); //0xFFFF87FF);
			this.put(CODE_214, XTERM_COLOR_214); //0xFFFFAF00);
			this.put(CODE_215, XTERM_COLOR_215); //0xFFFFAF5F);
			this.put(CODE_216, XTERM_COLOR_216); //0xFFFFAF87);
			this.put(CODE_217, XTERM_COLOR_217); //0xFFFFAFAF);
			this.put(CODE_218, XTERM_COLOR_218); //0xFFFFAFD7);
			this.put(CODE_219, XTERM_COLOR_219); //0xFFFFAFFF);
			this.put(CODE_220, XTERM_COLOR_220); //0xFFFFD700);
			this.put(CODE_221, XTERM_COLOR_221); //0xFFFFD75F);
			this.put(CODE_222, XTERM_COLOR_222); //0xFFFFD787);
			this.put(CODE_223, XTERM_COLOR_223); //0xFFFFD7AF);
			this.put(CODE_224, XTERM_COLOR_224); //0xFFFFD7D7);
			this.put(CODE_225, XTERM_COLOR_225); //0xFFFFD7FF);
			this.put(CODE_226, XTERM_COLOR_226); //0xFFFFFF00);
			this.put(CODE_227, XTERM_COLOR_227); //0xFFFFFF5F);
			this.put(CODE_228, XTERM_COLOR_228); //0xFFFFFF87);
			this.put(CODE_229, XTERM_COLOR_229); //0xFFFFFFAF);
			this.put(CODE_230, XTERM_COLOR_230); //0xFFFFFFD7);
			this.put(CODE_231, XTERM_COLOR_231); //0xFFFFFFFF);
			//blacks/greys
			this.put(CODE_232, XTERM_COLOR_232); //0xFF080808);
			this.put(CODE_233, XTERM_COLOR_233); //0xFF121212);
			this.put(CODE_234, XTERM_COLOR_234); //0xFF1C1C1C);
			this.put(CODE_235, XTERM_COLOR_235); //0xFF262626);
			this.put(CODE_236, XTERM_COLOR_236); //0xFF303030);
			this.put(CODE_237, XTERM_COLOR_237); //0xFF3A3A3A);
			this.put(CODE_238, XTERM_COLOR_238); //0xFF444444);
			this.put(CODE_239, XTERM_COLOR_239); //0xFF4E4E4E);
			this.put(CODE_240, XTERM_COLOR_240); //0xFF585858);
			this.put(CODE_241, XTERM_COLOR_241); //0xFF626262);
			this.put(CODE_242, XTERM_COLOR_242); //0xFF6C6C6C);
			this.put(CODE_243, XTERM_COLOR_243); //0xFF767676);
			this.put(CODE_244, XTERM_COLOR_244); //0xFF808080);
			this.put(CODE_245, XTERM_COLOR_245); //0xFF8A8A8A);
			this.put(CODE_246, XTERM_COLOR_246); //0xFF949494);
			this.put(CODE_247, XTERM_COLOR_247); //0xFF9E9E9E);
			this.put(CODE_248, XTERM_COLOR_248); //0xFFA8A8A8);
			this.put(CODE_249, XTERM_COLOR_249); //0xFFB2B2B2);
			this.put(CODE_250, XTERM_COLOR_250); //0xFFBCBCBC);
			this.put(CODE_251, XTERM_COLOR_251); //0xFFC6C6C6);
			this.put(CODE_252, XTERM_COLOR_252); //0xFFD0D0D0);
			this.put(CODE_253, XTERM_COLOR_253); //0xFFDADADA);
			this.put(CODE_254, XTERM_COLOR_254); //0xFFE4E4E4);
			this.put(CODE_255, XTERM_COLOR_255); //0xFFEEEEEE);
		}
	};
	/** This maps a string value to a corresponding integer value. */
	private static HashMap<CharSequence, Integer> colormap = new HashMap<CharSequence, Integer>();
	static
	{
		for (int i = 0; i < BYTE_MAX; i++) {
			colormap.put(Integer.toString(i), i);
		}
		
	}
	
	/** Private contructor to prevent instantiation. */
	private Colorizer() {		
		//i'm more of a static class.
	}
	
	/** Returns the color associated with the given code.
	 * 
	 * This is deprecate, this is for the old TextView markup.
	 * 
	 * @param bright 0 or 1, indicating brightness
	 * @param value 30-37 or 40-47, the ansi color code.
	 * @return The integer color value for the ansi code.
	 * @deprecated This is no longer used, hasn't been for a long time.
	 */
	@java.lang.Deprecated
	public static Object getColorCode(final Integer bright, final Integer value) {
		int colorval = MSB_MASK;
		
		int onespot = 0;
		int tenspot = 0;
		if (value >= ANSI_FOREGROUND_START && value < ANSI_BACKGROUND_START) {
			onespot = value - ANSI_FOREGROUND_START;
			tenspot = ANSI_FOREGROUND_TENSPOT;
		} else if (value >= ANSI_BACKGROUND_START && value < ANSI_BACKGROUND_MAX) {
			onespot = value - ANSI_BACKGROUND_START;
			tenspot = ANSI_BACKGROUND_TENSPOT;
		}
		
		if (bright == null || bright == 0) {
			//normal operation, not bright color
			switch(onespot) {
			case ANSI_BLACK_CODE:
				//black, nothing
				break;
			case ANSI_RED_CODE:
				//red
				colorval = colorval | ANSI_RED;
				break;
			case ANSI_GREEN_CODE:
				//green
				colorval = colorval | ANSI_GREEN;
				break;
			case ANSI_YELLOW_CODE: 
				//yellow
				colorval = colorval | ANSI_YELLOW;
				break;
			case ANSI_BLUE_CODE:
				//blue
				//colorval = colorval | 0xFF0000BB; //BB is too dark, turning it up, this hsould be an option.
				colorval = colorval | ANSI_BLUE;
				break;
			case ANSI_MAGENTA_CODE:
				//magenta
				colorval = colorval | ANSI_MAGENTA;
				break;
			case ANSI_CYAN_CODE:
				//cyan
				colorval = colorval | ANSI_CYAN;
				break;
			case ANSI_WHITE_CODE:
				//white
				colorval = colorval | ANSI_WHITE;
				break;
			default:
				break;
			}
			
		} else if (bright == 1) {
			//bright color operation
			switch(onespot) {
			case ANSI_BLACK_CODE:
				//black, 
				colorval = colorval | ANSI_BRIGHT_BLACK;
				break;
			case ANSI_RED_CODE:
				//red
				colorval = colorval | ANSI_BRIGHT_RED;
				break;
			case ANSI_GREEN_CODE:
				//green
				colorval = colorval | ANSI_BRIGHT_GREEN;
				break;
			case ANSI_YELLOW_CODE: 
				//yellow
				colorval = colorval | ANSI_BRIGHT_YELLOW;
				break;
			case ANSI_BLUE_CODE:
				//blue
				colorval = colorval | ANSI_BRIGHT_BLUE;
				break;
			case ANSI_MAGENTA_CODE:
				//magenta
				colorval = colorval | ANSI_BRIGHT_MAGENTA;
				break;
			case ANSI_CYAN_CODE:
				//cyan
				colorval = colorval | ANSI_BRIGHT_CYAN;
				break;
			case ANSI_WHITE_CODE:
				//white
				colorval = colorval | ANSI_BRIGHT_WHITE;
				break;
			default:
				break;
			}
		}
		
		if (tenspot == ANSI_FOREGROUND_TENSPOT) {
			return new ForegroundColorSpan(colorval);
		} else if (tenspot == ANSI_BACKGROUND_TENSPOT) {
			return new BackgroundColorSpan(colorval);
		} else {
			return null;
		}
	}
	
	
	/** Gets a color value for the given arguments.
	 * 
	 * @param bright 1 = bright, 0 = not bright
	 * @param value The color code to get
	 * @param is256Color Weather or not this code is an xterm 256 code.
	 * @return The integer color value.
	 */
	public static int getColorValue(final CharSequence bright, final CharSequence value, final boolean is256Color) {
		
		Integer b = colormap.get(bright.toString());
		Integer c = colormap.get(value.toString());
		
		if (b == null) {
			b = 0;
		}
		
		if (c == null) {
			c = ANSI_FOREGROUND_START + ANSI_RED_CODE;
		}
		
		return getColorValue(b, c, is256Color);
	}
	
	/** Enumeration of possible ansi color code types. */
	public enum COLOR_TYPE {
		/** 0, return to default. */
		ZERO_CODE,
		/** 1, sets brightness. */
		BRIGHT_CODE,
		/** 39, returns foreground to default. */
		DEFAULT_FOREGROUND,
		/** 49, returns the background to default. */
		DEFAULT_BACKGROUND,
		/** 1 or 0 I think, I'm not sure if this constant is used. */
		DIM_CODE,
		/** Background color, 40-48. */
		BACKGROUND,
		/** Foreground color, 30-38. */
		FOREGROUND,
		/** Anything that is not a real color. */
		NOT_A_COLOR,
		/** The xterm 256 foreground start marker, 38. */
		XTERM_256_FG_START,
		/** The xterm 256 background start marker. 48. */
		XTERM_256_BG_START,
		/** The xterm 256 extra marker byte 5. */
		XTERM_256_FIVE,
		/** An xterm 256 color. */
		XTERM_256_COLOR
	}
	
	/** Gets the type of color code for the given argument.
	 * 
	 * @param value The ansi color code to decode.
	 * @return The type of ansi color code given.
	 */
	public static COLOR_TYPE getColorType(final CharSequence value) {
		
		Integer c = colormap.get(value.toString());
		
		if (c == null) {
			return COLOR_TYPE.NOT_A_COLOR;
		}
		
		return getColorType(c);
	}

	/** Gets the type of color code for the given integer argument.
	 * 
	 * @param value The ansi color code to decode.
	 * @return The type of ansi color code for <b>value</b>
	 */
	public static COLOR_TYPE getColorType(final Integer value) {
		if (value == 0) {
			return COLOR_TYPE.ZERO_CODE;
		}
		
		if (value == 1) {
			return COLOR_TYPE.BRIGHT_CODE;
		}
		
		if (value == 2) {
			return COLOR_TYPE.DIM_CODE;
		}
		
		if (value == ANSI_FOREGROUND_DEFAULT) {
			return COLOR_TYPE.DEFAULT_FOREGROUND;
		}
		
		if (value == ANSI_BACKGROUND_DEFAULT) {
			return COLOR_TYPE.DEFAULT_BACKGROUND;
		}
		
		if (value == XTERM_256_FOREGROUND) { return COLOR_TYPE.XTERM_256_FG_START; }
		
		if (value == XTERM_256_BACKGROUND) { return COLOR_TYPE.XTERM_256_BG_START; }
		
		if (value == XTERM_256_FIVE) { return COLOR_TYPE.XTERM_256_FIVE; }
		
		COLOR_TYPE retval = COLOR_TYPE.NOT_A_COLOR;
		if (value < ANSI_BACKGROUND_START && value >= ANSI_FOREGROUND_START) {
			retval = COLOR_TYPE.FOREGROUND;
		} else if (value >= ANSI_BACKGROUND_START && value < ANSI_BACKGROUND_MAX) {
			retval = COLOR_TYPE.BACKGROUND;
		}
		
		//Log.e("Colorizer","Returning " + retval + " for " + value);
		
		return retval;
	}
	
	/** Returns a color value for an xterm 256 color code.
	 * 
	 * @param value The xterm 256 color code.
	 * @return The color for the code given by <b>value</b>
	 */
	public static int get256ColorValue(final Integer value) {
		int retVal = 0xFFFFFFFF;
		Integer val = colormap256.get(value);
		if (val != null) { retVal = val; }
		return retVal;
	}
		
	/** Returns a color value for a color code.
	 * 
	 * @param bright The brightness value. 0 or 1
	 * @param value The ansi color code to look up.
	 * @param is256color Weather <b>value</b> is an xterm 256 color code.
	 * @return The color value.
	 */
	public static int getColorValue(final Integer bright, final Integer value, final boolean is256color) {
		int colorval = 0x000000;
		
		if (is256color) { return get256ColorValue(value); }
		
		int onespot = 0;
		
		if (value == ANSI_FOREGROUND_DEFAULT) {
			return ANSI_WHITE;
		}
		if (value == ANSI_BACKGROUND_DEFAULT) {
			return 0x000000;
		}
		
		if (value >= ANSI_FOREGROUND_START && value < ANSI_BACKGROUND_START) {
			onespot = value - ANSI_FOREGROUND_START;
			//tenspot = 3;
		} else if (value >= ANSI_BACKGROUND_START && value < ANSI_BACKGROUND_MAX) {
			onespot = value - ANSI_BACKGROUND_START;
			//tenspot = 4;
		}
		
		if (bright == null || bright == 0) {
			//normal operation, not bright color
			switch(onespot) {
			case ANSI_BLACK_CODE:
				//black, nothing
				colorval = colorval & 0x000000;
				break;
			case ANSI_RED_CODE:
				//red
				colorval = colorval | ANSI_RED;
				break;
			case ANSI_GREEN_CODE:
				//green
				colorval = colorval | ANSI_GREEN;
				break;
			case ANSI_YELLOW_CODE: 
				//yellow
				colorval = colorval | ANSI_YELLOW;
				break;
			case ANSI_BLUE_CODE:
				//blue
				//colorval = colorval | 0x0000BB; //0x0000BB is a bit too dark on my screen, so i'm turning it up a bit. this should really be an option.
				colorval = colorval | ANSI_BLUE;
				break;
			case ANSI_MAGENTA_CODE:
				//magenta
				colorval = colorval | ANSI_MAGENTA;
				break;
			case ANSI_CYAN_CODE:
				//cyan
				colorval = colorval | ANSI_CYAN;
				break;
			case ANSI_WHITE_CODE:
				//white
				colorval = colorval | ANSI_WHITE;
				break;
			default:
				break;
			}
			
		} else if (bright == 1) {
			//bright color operation
			switch(onespot) {
			case ANSI_BLACK_CODE:
				//black, 
				colorval = colorval | ANSI_BRIGHT_BLACK;
				break;
			case ANSI_RED_CODE:
				//red
				colorval = colorval | ANSI_BRIGHT_RED;
				break;
			case ANSI_GREEN_CODE:
				//green
				colorval = colorval | ANSI_BRIGHT_GREEN;
				break;
			case ANSI_YELLOW_CODE: 
				//yellow
				colorval = colorval | ANSI_BRIGHT_YELLOW;
				break;
			case ANSI_BLUE_CODE:
				//blue
				colorval = colorval | ANSI_BRIGHT_BLUE;
				break;
			case ANSI_MAGENTA_CODE:
				//magenta
				colorval = colorval | ANSI_BRIGHT_MAGENTA;
				break;
			case ANSI_CYAN_CODE:
				//cyan
				colorval = colorval | ANSI_BRIGHT_CYAN;
				break;
			case ANSI_WHITE_CODE:
				//white
				colorval = colorval | ANSI_BRIGHT_WHITE;
				break;
			default:
				break;
			}
		}
		
		return colorval;
		
	}
	
	/** Returns a string that will produce red color. 
	 * 
	 * @return The color.
	 */
	public static String getRedColor() {
		return colorRed;
	}
	/** Returns a string that will produce white color.
	 * 
	 * @return The color
	 */
	public static String getWhiteColor() {
		return colorWhite;
	}
	/** Returns a string that will produce bright green color.
	 * 
	 * @return The color
	 */
	public static String getGreenColor() {
		return colorGreen;
	}
	/** Returns a string that will produce bright cyan color. 
	 * 
	 * @return The color
	 */
	public static String getBrightCyanColor() {
		return colorCyanBright;
	}
	/** Returns a string that will produce bright yellow color. 
	 * 
	 * @return The color
	 */
	public static String getBrightYellowColor() {
		return colorYeollowBright;
	}
	/** Returns a string that has bright yellow background and grey foreground. 
	 * 
	 * @return The ansi color sequence.
	 */
	public static String getTeloptStartColor() {
		return telOptColorBegin;
	}
	/** Returns a string that will reset ansi color processing to defaults.
	 * 
	 * @return The ansi color sequence.
	 */
	public static String getResetColor() {
		return telOptColorEnd;
	}


}
