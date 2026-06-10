// fp-shared.jsx — système partagé : palette, chapitres, code Scala, primitives de diagramme
// Style : manuel d'ingénierie sur papier quadrillé, esprit 3Blue1Brown.

const PAPER = '#f5f2ec';
const CARD  = '#fcfaf4';
const INK   = '#2b2a26';
const INK2  = '#7a7466';
const INK3  = '#aaa294';
const BLUE  = '#2a6fdb';
const GREEN = '#1f8a5b';
const RED   = '#c44536';
const AMBER = '#a8742a';

const SANS  = "'IBM Plex Sans', system-ui, sans-serif";
const SERIF = "'IBM Plex Serif', Georgia, serif";
const MONO  = "'IBM Plex Mono', ui-monospace, monospace";

// ── Chapitres (durées en secondes) ──────────────────────────────────────────
const CHAPTERS = [
  { id: 'intro',     num: '',   title: 'Ouverture',        sub: '',                                  dur: 22, color: INK   },
  { id: 'functor',   num: '01', title: 'Functor',          sub: 'transformer sans déballer',         dur: 40, color: BLUE  },
  { id: 'applic',    num: '02', title: 'Applicative',      sub: 'combiner des effets indépendants',  dur: 42, color: BLUE  },
  { id: 'monad',     num: '03', title: 'Monad',            sub: 'enchaîner des effets dépendants',   dur: 46, color: BLUE  },
  { id: 'either',    num: '04', title: 'Either',           sub: 'l’aiguillage du premier échec',     dur: 38, color: RED   },
  { id: 'validated', num: '05', title: 'Validated',        sub: 'accumuler toutes les erreurs',      dur: 40, color: RED   },
  { id: 'traverse',  num: '06', title: 'Traverse',         sub: 'retourner la structure',            dur: 42, color: GREEN },
  { id: 'state',     num: '07', title: 'State',            sub: 'l’état comme valeur',               dur: 42, color: GREEN },
  { id: 'kleisli',   num: '08', title: 'Kleisli',          sub: 'composer A => F[B]',                dur: 42, color: GREEN },
  { id: 'readert',   num: '09', title: 'ReaderT',          sub: 'l’injection de dépendances, pure',  dur: 42, color: GREEN },
  { id: 'funk',      num: '10', title: 'FunctionK',        sub: 'F ~> G · la transformation naturelle', dur: 44, color: AMBER },
  { id: 'tagless',   num: '11', title: 'Tagless Final',    sub: 'décrire ici, interpréter ailleurs', dur: 46, color: AMBER },
  { id: 'free',      num: '12', title: 'Free',             sub: 'le programme devient une donnée',   dur: 46, color: AMBER },
  { id: 'yoneda',    num: '13', title: 'Yoneda · Coyoneda', sub: 'fusionner les map',                dur: 44, color: AMBER },
  { id: 'finale',    num: '',   title: 'La carte',         sub: 'tout s’emboîte',                    dur: 44, color: INK   },
];
let __acc = 0;
CHAPTERS.forEach((c) => { c.start = __acc; __acc += c.dur; c.end = __acc; });
const TOTAL_DUR = __acc;
const CH = Object.fromEntries(CHAPTERS.map((c) => [c.id, c]));

// ── Fond papier quadrillé ───────────────────────────────────────────────────
function Paper() {
  return (
    <div style={{
      position: 'absolute', inset: 0, background: PAPER,
      backgroundImage: [
        'repeating-linear-gradient(0deg, rgba(43,42,38,0.05) 0 1px, transparent 1px 40px)',
        'repeating-linear-gradient(90deg, rgba(43,42,38,0.05) 0 1px, transparent 1px 40px)',
        'repeating-linear-gradient(0deg, rgba(43,42,38,0.07) 0 1px, transparent 1px 200px)',
        'repeating-linear-gradient(90deg, rgba(43,42,38,0.07) 0 1px, transparent 1px 200px)',
      ].join(', '),
    }}></div>
  );
}

// ── Coloration syntaxique Scala (légère, regex) ─────────────────────────────
const SCALA_KW = new Set(('val def trait given using extension enum case match type opaque object new if then ' +
  'else for yield sealed final class import infix with end derives private lazy override extends this').split(' '));
const HL_RE = /(\/\/.*$)|("(?:[^"\\]|\\.)*")|(\b\d[\d._]*\b)|(\b[a-zA-Z_][\w]*\b)|(=>|~>|<-|>=>|::|\+\+)|(.)/gm;

function hlScala(line) {
  const out = [];
  let m, k = 0;
  HL_RE.lastIndex = 0;
  while ((m = HL_RE.exec(line)) !== null) {
    const [tok, com, str, num, word, op] = m;
    let color = INK, weight = 400, style = 'normal';
    if (com != null) { color = INK3; style = 'italic'; }
    else if (str != null) { color = GREEN; }
    else if (num != null) { color = AMBER; }
    else if (word != null) {
      if (SCALA_KW.has(word)) { color = '#a8402f'; weight = 600; }
      else if (/^[A-Z]/.test(word)) { color = BLUE; }
    }
    else if (op != null) { color = '#8a5fb0'; weight = 600; }
    out.push(<span key={k++} style={{ color, fontWeight: weight, fontStyle: style }}>{tok}</span>);
    if (m.index === HL_RE.lastIndex) HL_RE.lastIndex++;
  }
  return out;
}

// ── Panneau de code ─────────────────────────────────────────────────────────
// lines: Array<string | {s: string, hl?: color}> — révélation ligne à ligne.
function CodePanel({ title, lines, t, start = 0, x = 1120, y = 190, w = 740, fontSize = 22, lps = 2.4 }) {
  const slide = animate({ from: 70, to: 0, start, end: start + 0.7, ease: Easing.easeOutCubic })(t);
  const op = animate({ from: 0, to: 1, start, end: start + 0.5 })(t);
  if (op <= 0) return null;
  const lh = fontSize * 1.5;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: w,
      transform: `translateX(${slide}px)`, opacity: op,
      background: CARD, border: `1.5px solid ${INK}`, borderRadius: 10,
      boxShadow: '5px 6px 0 rgba(43,42,38,0.12)', overflow: 'hidden',
    }}>
      <div style={{
        display: 'flex', alignItems: 'center', gap: 10, padding: '10px 18px',
        borderBottom: `1.5px solid ${INK}`, background: 'rgba(43,42,38,0.045)',
        fontFamily: MONO, fontSize: 17, color: INK2, letterSpacing: '0.04em',
      }}>
        <span style={{ width: 9, height: 9, borderRadius: 5, background: INK3, display: 'inline-block' }}></span>
        <span>{title}</span>
      </div>
      <div style={{ padding: '16px 22px 18px' }}>
        {lines.map((ln, i) => {
          const obj = typeof ln === 'string' ? { s: ln } : ln;
          const at = start + 0.4 + i / lps;
          const p = animate({ from: 0, to: 1, start: at, end: at + 0.35, ease: Easing.easeOutCubic })(t);
          if (p <= 0) return <div key={i} style={{ height: lh }}></div>;
          return (
            <div key={i} style={{
              fontFamily: MONO, fontSize, lineHeight: `${lh}px`, whiteSpace: 'pre',
              opacity: p, transform: `translateY(${(1 - p) * 8}px)`,
              background: obj.hl ? `color-mix(in oklab, ${obj.hl} 14%, transparent)` : 'transparent',
              margin: '0 -12px', padding: '0 12px', borderRadius: 5,
            }}>
              {obj.s === '' ? ' ' : hlScala(obj.s)}
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ── Primitives de diagramme ─────────────────────────────────────────────────
const pop = (t, delay, dur = 0.5) => {
  const p = clamp((t - delay) / dur, 0, 1);
  return { p, op: Math.min(1, p * 2), sc: 0.55 + 0.45 * Easing.easeOutBack(p) };
};

// Boîte-conteneur (F[A]) avec étiquette technique sur le bord
function Box({ x, y, w, h, label, color = INK, t = 1, delay = 0, dashed = false, fill, labelSize = 21, children }) {
  const { op, sc } = pop(t, delay);
  if (op <= 0) return null;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: w, height: h,
      border: `2.5px ${dashed ? 'dashed' : 'solid'} ${color}`, borderRadius: 16,
      background: fill || 'rgba(252,250,244,0.75)',
      opacity: op, transform: `scale(${sc})`, transformOrigin: 'center',
    }}>
      {label && (
        <div style={{
          position: 'absolute', top: -15, left: 18, background: PAPER, padding: '0 10px',
          fontFamily: MONO, fontSize: labelSize, fontWeight: 600, color, letterSpacing: '0.02em', whiteSpace: 'nowrap',
        }}>{label}</div>
      )}
      {children}
    </div>
  );
}

// Jeton de valeur (cercle + libellé mono), avec léger flottement
function Token({ x, y, r = 36, label, color = INK, t = 1, delay = 0, float = true, fill = '#fff', fontSize = 24 }) {
  const { op, sc } = pop(t, delay);
  if (op <= 0) return null;
  const dy = float ? 3 * Math.sin(t * 1.4 + x * 0.013) : 0;
  return (
    <div style={{
      position: 'absolute', left: x - r, top: y - r + dy, width: r * 2, height: r * 2,
      borderRadius: r, border: `2.5px solid ${color}`, background: fill,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: MONO, fontSize, fontWeight: 600, color,
      opacity: op, transform: `scale(${sc})`,
    }}>{label}</div>
  );
}

// Flèche SVG qui se dessine (quadratique, pointe orientée)
function Arrow({ x1, y1, x2, y2, bend = 0, color = INK, t = 1, delay = 0, dur = 0.7,
                 width = 3, dashed = false, label, labelDx = 0, labelDy = -16, labelColor, labelSize = 22 }) {
  const p = clamp((t - delay) / dur, 0, 1);
  if (p <= 0) return null;
  const e = Easing.easeInOutCubic(p);
  const mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
  const dx = x2 - x1, dy = y2 - y1;
  const len = Math.hypot(dx, dy) || 1;
  const nx = -dy / len, ny = dx / len;
  const cx = mx + nx * bend, cy = my + ny * bend;
  const pad = 30;
  const minX = Math.min(x1, x2, cx) - pad, minY = Math.min(y1, y2, cy) - pad;
  const W = Math.max(x1, x2, cx) - minX + pad, H = Math.max(y1, y2, cy) - minY + pad;
  const ang = Math.atan2(y2 - cy, x2 - cx) * 180 / Math.PI;
  const headOp = clamp((p - 0.65) / 0.3, 0, 1);
  return (
    <svg style={{ position: 'absolute', left: minX, top: minY, overflow: 'visible', pointerEvents: 'none' }} width={W} height={H}>
      <path
        d={`M ${x1 - minX} ${y1 - minY} Q ${cx - minX} ${cy - minY} ${x2 - minX} ${y2 - minY}`}
        fill="none" stroke={color} strokeWidth={width} strokeLinecap="round"
        pathLength="1"
        strokeDasharray={dashed ? '0.045 0.03' : '1 1'}
        strokeDashoffset={dashed ? 0 : 1 - e}
        opacity={dashed ? e : 1}
      ></path>
      <g transform={`translate(${x2 - minX}, ${y2 - minY}) rotate(${ang})`} opacity={headOp}>
        <path d="M -14 -8 L 2 0 L -14 8" fill="none" stroke={color} strokeWidth={width} strokeLinecap="round" strokeLinejoin="round"></path>
      </g>
      {label && (
        <text x={cx - minX + labelDx} y={cy - minY + labelDy} textAnchor="middle"
          fontFamily={MONO} fontSize={labelSize} fontWeight="600" fill={labelColor || color} opacity={headOp}>
          {label}
        </text>
      )}
    </svg>
  );
}

// Légende / texte explicatif avec fenêtre temporelle [from, to]
function Cap({ t, from = 0, to = Infinity, x, y, w = 800, align = 'center', size = 30, color = INK,
               font = SANS, weight = 500, italic = false, children }) {
  const op = Math.min(
    animate({ from: 0, to: 1, start: from, end: from + 0.5 })(t),
    to === Infinity ? 1 : animate({ from: 1, to: 0, start: to - 0.4, end: to })(t)
  );
  if (op <= 0) return null;
  const ty = (1 - animate({ from: 0, to: 1, start: from, end: from + 0.5, ease: Easing.easeOutCubic })(t)) * 14;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: w, textAlign: align,
      fontFamily: font, fontSize: size, fontWeight: weight, color, lineHeight: 1.35,
      fontStyle: italic ? 'italic' : 'normal',
      opacity: op, transform: `translateY(${ty}px)`, textWrap: 'pretty',
    }}>{children}</div>
  );
}

// Petit libellé technique mono (annotations de schéma)
function Tag({ t, from = 0, to = Infinity, x, y, color = INK2, size = 20, align = 'center', w = 400, children }) {
  return (
    <Cap t={t} from={from} to={to} x={x} y={y} w={w} align={align} size={size} color={color} font={MONO} weight={500}>
      {children}
    </Cap>
  );
}

// Croix d'erreur ou coche, dessinée
function Mark({ x, y, kind = 'check', color, t = 1, delay = 0, size = 36 }) {
  const p = clamp((t - delay) / 0.4, 0, 1);
  if (p <= 0) return null;
  const c = color || (kind === 'check' ? GREEN : RED);
  const s = size;
  return (
    <svg style={{ position: 'absolute', left: x - s / 2, top: y - s / 2, overflow: 'visible' }} width={s} height={s} viewBox="0 0 36 36">
      {kind === 'check' ? (
        <path d="M 6 19 L 15 28 L 30 8" fill="none" stroke={c} strokeWidth="4.5" strokeLinecap="round" strokeLinejoin="round"
          pathLength="1" strokeDasharray="1 1" strokeDashoffset={1 - Easing.easeOutCubic(p)}></path>
      ) : (
        <g>
          <path d="M 8 8 L 28 28" fill="none" stroke={c} strokeWidth="4.5" strokeLinecap="round"
            pathLength="1" strokeDasharray="1 1" strokeDashoffset={1 - clamp(p * 2, 0, 1)}></path>
          <path d="M 28 8 L 8 28" fill="none" stroke={c} strokeWidth="4.5" strokeLinecap="round"
            pathLength="1" strokeDasharray="1 1" strokeDashoffset={1 - clamp(p * 2 - 0.6, 0, 1)}></path>
        </g>
      )}
    </svg>
  );
}

// ── Coquille de scène standard ──────────────────────────────────────────────
// Carte-titre centrée (0 → ~3 s), puis puce de chapitre en haut à gauche.
// Le diagramme occupe tout l'écran, puis se compacte à gauche quand le code arrive.
// Bandeau « à retenir » en bas. Fondu de sortie global.
function SceneShell({ ch, code, codeTitle, codeAt, takeaway, takeawayAt, codeX = 1120, codeY = 190,
                      codeW = 740, codeFontSize = 22, compactScale = 0.56, compactX = 30, compactY = 215, children }) {
  const { localTime: t, duration: D } = useSprite();
  const tCode = codeAt != null ? codeAt : D * 0.52;
  const tKey = takeawayAt != null ? takeawayAt : D - 7.5;
  const fadeOut = animate({ from: 1, to: 0, start: D - 0.7, end: D - 0.05 })(t);

  // Carte-titre
  const titleOp = Math.min(
    animate({ from: 0, to: 1, start: 0.1, end: 0.6 })(t),
    animate({ from: 1, to: 0, start: 2.3, end: 2.9 })(t)
  );
  const chipOp = animate({ from: 0, to: 1, start: 2.7, end: 3.2 })(t);

  // Compactage du diagramme à l'arrivée du code
  const cp = code ? animate({ from: 0, to: 1, start: tCode - 0.2, end: tCode + 0.7 })(t) : 0;
  const dScale = 1 - (1 - compactScale) * cp;
  const dX = compactX * cp, dY = compactY * cp;

  const keyOp = animate({ from: 0, to: 1, start: tKey, end: tKey + 0.6 })(t);
  const keyW = animate({ from: 0, to: 100, start: tKey + 0.2, end: tKey + 1.1, ease: Easing.easeOutCubic })(t);

  return (
    <div style={{ position: 'absolute', inset: 0, opacity: fadeOut }}>
      {/* Diagramme */}
      <div style={{
        position: 'absolute', inset: 0,
        transform: `translate(${dX}px, ${dY}px) scale(${dScale})`,
        transformOrigin: '0 0',
      }}>
        {typeof children === 'function' ? children({ t, D, tCode, compact: cp }) : children}
      </div>

      {/* Carte-titre centrée */}
      {titleOp > 0 && (
        <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', opacity: titleOp, background: PAPER }}>
          <div style={{ textAlign: 'center', transform: `translateY(${(1 - Math.min(1, t / 0.7)) * 20}px)` }}>
            <div style={{ fontFamily: MONO, fontSize: 30, color: ch.color, fontWeight: 600, letterSpacing: '0.25em', marginBottom: 18 }}>
              {ch.num ? `№ ${ch.num}` : ''}
            </div>
            <div style={{ fontFamily: SERIF, fontSize: 110, fontWeight: 600, color: INK, letterSpacing: '-0.01em', lineHeight: 1 }}>
              {ch.title}
            </div>
            <div style={{ marginTop: 26, fontFamily: SANS, fontSize: 34, fontStyle: 'italic', color: INK2 }}>
              {ch.sub}
            </div>
            <svg width="340" height="14" style={{ marginTop: 24, overflow: 'visible' }}>
              <path d={`M 4 8 Q 170 ${2 + 5 * Math.sin(t * 2)} 336 7`} fill="none" stroke={ch.color} strokeWidth="3" strokeLinecap="round"
                pathLength="1" strokeDasharray="1 1" strokeDashoffset={1 - animate({ from: 0, to: 1, start: 0.5, end: 1.3, ease: Easing.easeInOutCubic })(t)}></path>
            </svg>
          </div>
        </div>
      )}

      {/* Puce de chapitre */}
      <div style={{
        position: 'absolute', top: 44, left: 60, display: 'flex', alignItems: 'baseline', gap: 16, whiteSpace: 'nowrap',
        opacity: chipOp, transform: `translateY(${(1 - chipOp) * -10}px)`,
      }}>
        <span style={{ fontFamily: MONO, fontSize: 24, color: ch.color, fontWeight: 600 }}>{ch.num || '—'}</span>
        <span style={{ fontFamily: SERIF, fontSize: 38, fontWeight: 600, color: INK }}>{ch.title}</span>
        <span style={{ fontFamily: SANS, fontSize: 24, fontStyle: 'italic', color: INK2 }}>— {ch.sub}</span>
      </div>

      {/* Code */}
      {code && t >= tCode && (
        <CodePanel title={codeTitle} lines={code} t={t} start={tCode} x={codeX} y={codeY} w={codeW} fontSize={codeFontSize} />
      )}

      {/* À retenir */}
      {takeaway && keyOp > 0 && (
        <div style={{ position: 'absolute', left: 0, right: 0, bottom: 38, display: 'flex', justifyContent: 'center', opacity: keyOp }}>
          <div style={{ textAlign: 'center', maxWidth: 1500 }}>
            <span style={{ fontFamily: MONO, fontSize: 19, letterSpacing: '0.22em', color: ch.color, fontWeight: 600 }}>À RETENIR — </span>
            <span style={{ fontFamily: SANS, fontSize: 29, fontWeight: 600, color: INK }}>{takeaway}</span>
            <div style={{ height: 3, background: ch.color, width: `${keyW}%`, margin: '10px auto 0', borderRadius: 2, opacity: 0.85 }}></div>
          </div>
        </div>
      )}
    </div>
  );
}

Object.assign(window, {
  PAPER, CARD, INK, INK2, INK3, BLUE, GREEN, RED, AMBER, SANS, SERIF, MONO,
  CHAPTERS, CH, TOTAL_DUR,
  Paper, hlScala, CodePanel, Box, Token, Arrow, Cap, Tag, Mark, SceneShell, pop,
});
