// shared.jsx — 共享视觉词汇：调色板、字幕、章节牌、箭头、节点、容器盒、代码块
// 颜色语义（全片一致）：范畴=绿 / 函子=蓝 / 幺半群=金 / 单子=珊瑚

const MV = {
  bg: '#0d1117',
  panel: 'rgba(255,255,255,0.035)',
  ink: '#e9eef5',
  dim: '#93a0b4',
  faint: '#5b6678',
  line: 'rgba(233,238,245,0.16)',
  cat: '#7fcb8f',     // 范畴 green
  fun: '#58c4dd',     // 函子 blue
  mon: '#f0c674',     // 幺半群 gold
  mnd: '#ec8a6a',     // 单子 coral
  pur: '#b48edc',     // 辅助 purple（自然变换）
  serif: "'Noto Serif SC', serif",
  sans: "'Noto Sans SC', sans-serif",
  mono: "'JetBrains Mono', ui-monospace, monospace",
};

const LOCALE = new URLSearchParams(window.location.search).get('lang') === 'fr' ? 'fr' : 'zh';
document.documentElement.lang = LOCALE === 'fr' ? 'fr' : 'zh-CN';
document.title = LOCALE === 'fr'
  ? 'Monade : un monoïde dans la catégorie des endofoncteurs · Présentation animée'
  : '单子：自函子范畴上的幺半群 · 动画讲解';

function isFr() { return LOCALE === 'fr'; }
function tr(zh, fr) { return isFr() && fr != null ? fr : zh; }
function localeHref(locale) {
  const url = new URL(window.location.href);
  url.searchParams.set('lang', locale);
  return url.href;
}

function useT() { return useTime(); }

// 0→1 进度：从绝对时刻 start 起，历时 dur
function ev(t, start, dur, fn = Easing.easeInOutCubic) {
  return fn(clamp((t - start) / dur, 0, 1));
}
// 进出场透明度：from..to 窗口内，头尾各 enter/exit 秒
function fadeIO(t, from, to, enter = 0.5, exit = 0.5) {
  const a = clamp((t - from) / enter, 0, 1);
  const b = clamp((to - t) / exit, 0, 1);
  return Math.min(a, b);
}

// ── 整组内容的进出场容器 ──────────────────────────────────────
function FadeGroup({ from, to, enter = 0.6, exit = 0.6, drift = 14, style, children }) {
  const t = useT();
  if (t < from || t > to) return null;
  const aIn = Easing.easeOutCubic(clamp((t - from) / enter, 0, 1));
  const aOut = Easing.easeInCubic(clamp((to - t) / exit, 0, 1));
  const o = Math.min(aIn, aOut);
  const y = (1 - aIn) * drift;
  return (
    <div style={{ position: 'absolute', inset: 0, opacity: o, transform: `translateY(${y}px)`, ...style }}>
      {children}
    </div>
  );
}

// ── 底部解说字幕 ──────────────────────────────────────────────
function Cap({ from, to, children, fr }) {
  const t = useT();
  if (t < from || t > to) return null;
  const o = fadeIO(t, from, to, 0.4, 0.35);
  const y = (1 - Easing.easeOutCubic(clamp((t - from) / 0.4, 0, 1))) * 10;
  const content = tr(children, fr);
  return (
    <div style={{
      position: 'absolute', left: '50%', bottom: 46, transform: `translate(-50%, ${y}px)`,
      width: isFr() ? 1660 : 1560, textAlign: 'center', opacity: o,
      fontFamily: MV.sans, fontSize: isFr() ? 29 : 31, fontWeight: 400,
      lineHeight: isFr() ? 1.35 : 1.55,
      color: '#d9e1ec', letterSpacing: isFr() ? 0 : '0.02em',
      textShadow: '0 2px 18px rgba(0,0,0,0.8)',
      textWrap: 'pretty',
    }}>{content}</div>
  );
}
// 字幕内强调色
function Em({ c = MV.mon, children }) {
  return <span style={{ color: c, fontWeight: 500 }}>{children}</span>;
}

// ── 章节牌（顶部）──────────────────────────────────────────────
function Chapter({ from, to, num, zh, en, fr, color }) {
  const t = useT();
  if (t < from || t > to) return null;
  const o = fadeIO(t, from, to, 0.8, 0.6);
  const w = ev(t, from, 1.0) * 56;
  const title = tr(zh, fr);
  return (
    <div style={{ position: 'absolute', top: 54, left: 0, right: 0, opacity: o, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 10 }}>
      <div style={{ fontFamily: MV.mono, fontSize: 19, letterSpacing: '0.32em', color: MV.faint, textTransform: 'uppercase', whiteSpace: 'nowrap' }}>{num}</div>
      <div style={{
        fontFamily: isFr() ? MV.sans : MV.serif,
        fontSize: isFr() ? 40 : 44,
        fontWeight: 700,
        color: MV.ink,
        letterSpacing: isFr() ? '0.01em' : '0.06em',
        whiteSpace: 'nowrap',
      }}>
        {title} {en ? <span style={{ color: color, fontWeight: 600, fontFamily: MV.mono, fontSize: 30, letterSpacing: '0.02em', marginLeft: 10 }}>{en}</span> : null}
      </div>
      <div style={{ width: w, height: 3, background: color, borderRadius: 2 }}></div>
    </div>
  );
}

// ── SVG 画层 ──────────────────────────────────────────────────
function SvgLayer({ children, style }) {
  return (
    <svg width="1920" height="1080" viewBox="0 0 1920 1080"
      style={{ position: 'absolute', inset: 0, overflow: 'visible', pointerEvents: 'none', ...style }}>
      {children}
    </svg>
  );
}

// 二次贝塞尔箭头：from(start) 起 dur 秒画出；bend 为垂直偏移（正=左手侧）
function Arrow({ x1, y1, x2, y2, bend = 0, color = MV.ink, width = 4, from = 0, dur = 0.8,
                 label, labelDy = -16, labelSize = 24, dashed = false, opacity = 1, shrink = 26 }) {
  const t = useT();
  const p = ev(t, from, dur, Easing.easeInOutCubic);
  if (p <= 0 || opacity <= 0) return null;
  // 两端各收缩 shrink px，避免压住节点
  const dx = x2 - x1, dy = y2 - y1;
  const len = Math.hypot(dx, dy) || 1;
  const ux = dx / len, uy = dy / len;
  const ax1 = x1 + ux * shrink, ay1 = y1 + uy * shrink;
  const ax2 = x2 - ux * shrink, ay2 = y2 - uy * shrink;
  // 控制点：中点 + 法向 bend
  const mx = (ax1 + ax2) / 2 - uy * bend, my = (ay1 + ay2) / 2 + ux * bend;
  const d = `M ${ax1} ${ay1} Q ${mx} ${my} ${ax2} ${ay2}`;
  // 末端切向（控制点→终点）
  const tx = ax2 - mx, ty = ay2 - my;
  const ta = Math.atan2(ty, tx) * 180 / Math.PI;
  // 标签放在曲线中点
  const lx = 0.25 * ax1 + 0.5 * mx + 0.25 * ax2;
  const ly = 0.25 * ay1 + 0.5 * my + 0.25 * ay2;
  const headO = p > 0.92 ? (p - 0.92) / 0.08 : 0;
  return (
    <g opacity={opacity}>
      <path d={d} fill="none" stroke={color} strokeWidth={width}
        strokeLinecap="round" strokeDasharray={dashed ? '2 10' : '1'} pathLength={dashed ? 100 : 1}
        strokeDashoffset={dashed ? (1 - p) * 100 : (1 - p)} />
      <g transform={`translate(${ax2} ${ay2}) rotate(${ta})`} opacity={headO}>
        <path d="M 2 0 L -16 -9 L -11 0 L -16 9 Z" fill={color} />
      </g>
      {label ? (
        <text x={lx} y={ly + labelDy} textAnchor="middle" opacity={p > 0.5 ? (p - 0.5) * 2 : 0}
          fontFamily={MV.mono} fontSize={labelSize} fill={color}
          style={{ paintOrder: 'stroke', stroke: MV.bg, strokeWidth: 10, strokeLinejoin: 'round' }}>
          {label}
        </text>
      ) : null}
    </g>
  );
}

// 节点自环（identity）：节点上方一个小圆圈箭头
function IdLoop({ x, y, r = 34, color = MV.dim, from = 0, dur = 0.7, label = 'id', opacity = 1 }) {
  const t = useT();
  const p = ev(t, from, dur, Easing.easeInOutCubic);
  if (p <= 0) return null;
  const cy = y - 54;
  const circ = 2 * Math.PI * r;
  return (
    <g opacity={opacity}>
      <circle cx={x} cy={cy} r={r} fill="none" stroke={color} strokeWidth={3.5}
        strokeLinecap="round" strokeDasharray={circ} strokeDashoffset={circ * (1 - p * 0.92)}
        transform={`rotate(118 ${x} ${cy})`} />
      {p > 0.85 ? (
        <g transform={`translate(${x - r * Math.cos(0.4)} ${cy + r * Math.sin(0.4)}) rotate(245)`} opacity={(p - 0.85) / 0.15}>
          <path d="M 2 0 L -13 -8 L -9 0 L -13 8 Z" fill={color} />
        </g>
      ) : null}
      <text x={x} y={cy - r - 12} textAnchor="middle" opacity={p}
        fontFamily={MV.mono} fontSize={21} fill={color}>{label}</text>
    </g>
  );
}

// ── 类型节点（HTML）──────────────────────────────────────────
function NodeDot({ x, y, label, color = MV.cat, from = 0, size = 17, fontSize = 27, dimLabel = false, opacity = 1 }) {
  const t = useT();
  const p = ev(t, from, 0.55, Easing.easeOutBack);
  if (p <= 0) return null;
  return (
    <div style={{ position: 'absolute', left: x, top: y, transform: 'translate(-50%,-50%)', opacity, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 13 }}>
      <div style={{
        width: size, height: size, borderRadius: '50%', background: color,
        transform: `scale(${p})`, boxShadow: `0 0 22px ${color}66, 0 0 60px ${color}2a`,
      }}></div>
      <div style={{
        fontFamily: MV.mono, fontSize, fontWeight: 500, whiteSpace: 'nowrap',
        color: dimLabel ? MV.dim : MV.ink, opacity: Math.min(1, p * 1.2),
      }}>{label}</div>
    </div>
  );
}

// ── 容器盒（表示 F[_]）────────────────────────────────────────
function BoxC({ x, y, w, h, color = MV.fun, label, from = 0, dur = 0.6, opacity = 1, fill = true, labelSize = 22, children, style }) {
  const t = useT();
  const p = ev(t, from, dur, Easing.easeOutBack);
  if (p <= 0) return null;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: w, height: h,
      border: `3px solid ${color}`, borderRadius: 18,
      background: fill ? `${color}14` : 'transparent',
      opacity: opacity * Math.min(1, p * 1.4),
      transform: `scale(${0.85 + 0.15 * p})`,
      boxShadow: `0 0 34px ${color}22`,
      ...style,
    }}>
      {label ? (
        <div style={{
          position: 'absolute', top: -15, left: 20, padding: '0 10px', background: MV.bg,
          fontFamily: MV.mono, fontSize: labelSize, fontWeight: 600, color, whiteSpace: 'nowrap',
        }}>{label}</div>
      ) : null}
      {children}
    </div>
  );
}

// 圆形小值（裸值 42 之类）
function ValDot({ x, y, label, color = MV.ink, bg = '#202938', from = 0, size = 74, fontSize = 28, opacity = 1, style }) {
  const t = useT();
  const p = ev(t, from, 0.5, Easing.easeOutBack);
  if (p <= 0) return null;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: size, height: size, transform: `translate(-50%,-50%) scale(${p})`,
      borderRadius: '50%', background: bg, border: `2.5px solid ${color}88`,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: MV.mono, fontSize, fontWeight: 600, color: MV.ink, opacity,
      boxShadow: '0 4px 18px rgba(0,0,0,0.4)',
      ...style,
    }}>{label}</div>
  );
}

// ── 代码块 ────────────────────────────────────────────────────
// lines: 数组，每行是 [text, colorKey] 片段数组；colorKey ∈ kw/ty/fn/st/cm/pl/dim
const CODE_COLORS = {
  kw: '#c792ea', ty: '#58c4dd', fn: '#f0c674', st: '#9ece8c',
  cm: '#5b6678', pl: '#dbe3ee', dim: '#7d8a9e', op: '#ec8a6a',
};
function CodeBlock({ x, y, lines, from = 0, perLine = 0.18, size = 27, width, title, opacity = 1, lineFrom }) {
  const t = useT();
  const p0 = ev(t, from, 0.5, Easing.easeOutCubic);
  if (p0 <= 0) return null;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width,
      background: '#11161f', border: `1.5px solid ${MV.line}`, borderRadius: 16,
      padding: '26px 36px', opacity: opacity * p0, transform: `translateY(${(1 - p0) * 14}px)`,
      boxShadow: '0 18px 50px rgba(0,0,0,0.45)',
      fontFamily: MV.mono, fontSize: size, lineHeight: 1.62,
    }}>
      {title ? (
        <div style={{ fontFamily: MV.mono, fontSize: size * 0.62, color: MV.faint, letterSpacing: '0.18em', marginBottom: 12, textTransform: 'uppercase' }}>{title}</div>
      ) : null}
      {lines.map((segs, i) => {
        const lf = lineFrom && lineFrom[i] != null ? lineFrom[i] : from + 0.25 + i * perLine;
        const lp = ev(t, lf, 0.4, Easing.easeOutCubic);
        return (
          <div key={i} style={{ opacity: lp, transform: `translateX(${(1 - lp) * 10}px)`, whiteSpace: 'pre', minHeight: '1.62em' }}>
            {segs.map((s, j) => (
              <span key={j} style={{ color: CODE_COLORS[s[1] || 'pl'] }}>{s[0]}</span>
            ))}
          </div>
        );
      })}
    </div>
  );
}

// 小徽章（Option[_] 之类）
function Badge({ x, y, label, color = MV.fun, from = 0, fontSize = 26, opacity = 1 }) {
  const t = useT();
  const p = ev(t, from, 0.5, Easing.easeOutBack);
  if (p <= 0) return null;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, transform: `translate(-50%,-50%) scale(${0.7 + 0.3 * p})`,
      padding: '10px 24px', borderRadius: 999, border: `2px solid ${color}`,
      background: `${color}14`, color, fontFamily: MV.mono, fontSize, fontWeight: 600,
      whiteSpace: 'nowrap', opacity: opacity * Math.min(1, p * 1.3),
      boxShadow: `0 0 26px ${color}26`,
    }}>{label}</div>
  );
}

// 时间戳标签：每秒更新 data-screen-label，便于按时刻评论
function LocaleSwitch() {
  const choices = [
    { locale: 'zh', label: '中文' },
    { locale: 'fr', label: 'FR' },
  ];
  return (
    <div style={{
      position: 'absolute', top: 22, right: 26, zIndex: 20,
      display: 'flex', gap: 6, padding: 5,
      border: `1px solid ${MV.line}`, borderRadius: 999,
      background: 'rgba(13,17,23,0.72)', backdropFilter: 'blur(8px)',
      fontFamily: MV.mono, fontSize: 15,
    }}>
      {choices.map((choice) => {
        const active = LOCALE === choice.locale;
        return (
          <a key={choice.locale} href={localeHref(choice.locale)} title={choice.locale === 'fr' ? 'lang=fr' : 'lang=zh'} style={{
            color: active ? MV.bg : MV.dim,
            background: active ? MV.ink : 'transparent',
            textDecoration: 'none',
            borderRadius: 999,
            padding: '7px 12px',
            lineHeight: 1,
            fontWeight: 700,
          }}>{choice.label}</a>
        );
      })}
    </div>
  );
}

function Labeler({ scenes, children }) {
  const t = useT();
  const sec = Math.floor(t);
  const mm = String(Math.floor(sec / 60)).padStart(2, '0');
  const ss = String(sec % 60).padStart(2, '0');
  let name = '';
  for (const s of scenes) { if (t >= s.t) name = tr(s.name, s.fr); }
  return (
    <div data-screen-label={`${name} · ${mm}:${ss}`} style={{ position: 'absolute', inset: 0 }}>
      {children}
    </div>
  );
}

Object.assign(window, {
  MV, LOCALE, isFr, tr, useT, ev, fadeIO, FadeGroup, Cap, Em, Chapter, SvgLayer, Arrow, IdLoop,
  NodeDot, BoxC, ValDot, CodeBlock, CODE_COLORS, Badge, LocaleSwitch, Labeler,
});
