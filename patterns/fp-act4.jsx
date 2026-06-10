// fp-act4.jsx — Tagless Final, Free, Yoneda/Coyoneda, et la carte finale

// ── 11 · Tagless Final ──────────────────────────────────────────────────────
const CODE_TAGLESS = [
  'trait KVStore[F[_]]:                  // l’algèbre',
  '  def get(key: String): F[Option[String]]',
  '  def put(key: String, v: String): F[Unit]',
  '',
  'def program[F[_]: Monad]',
  '           (using kv: KVStore[F]): F[Unit] =',
  '  for',
  '    _ <- kv.put("hello", "world")',
  '    v <- kv.get("hello")',
  '  yield ()',
  '',
  { s: 'given KVStore[IO]   = LiveStore(redis) // prod', hl: GREEN },
  { s: 'given KVStore[Test] = InMemoryStore()  // tests', hl: BLUE },
];

function ProgramCard({ x, y, w, t, delay, title, lines, color = INK }) {
  const { op, sc } = pop(t, delay);
  if (op <= 0) return null;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: w, opacity: op, transform: `scale(${sc})`,
      background: CARD, border: `2.5px solid ${color}`, borderRadius: 12,
      boxShadow: '4px 5px 0 rgba(43,42,38,0.12)', overflow: 'hidden',
    }}>
      <div style={{ padding: '8px 18px', borderBottom: `2px solid ${color}`, fontFamily: MONO, fontSize: 20, fontWeight: 600, color }}>
        {title}
      </div>
      <div style={{ padding: '12px 18px' }}>
        {lines.map((l, i) => (
          <div key={i} style={{ fontFamily: MONO, fontSize: 19, lineHeight: '30px', whiteSpace: 'pre', color: INK }}>{hlScala(l)}</div>
        ))}
      </div>
    </div>
  );
}

function SceneTagless() {
  return (
    <SceneShell ch={CH.tagless} code={CODE_TAGLESS} codeTitle="TaglessFinal.scala" codeFontSize={20}
      takeaway="Le programme décrit — l’interpréteur décide. F reste abstrait jusqu’au bout.">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          {/* le programme abstrait */}
          <ProgramCard x={640} y={250} w={620} t={t} delay={3.0} title="program[F[_]] — abstrait" lines={[
            'kv.put("hello", "world")',
            'kv.get("hello")',
          ]} />
          <Cap t={t} from={4.6} to={8.8} x={510} y={460} w={900} size={30} italic={true} color={INK2}>
            écrit une seule fois — il ne sait pas <b>qui</b> est F
          </Cap>

          {/* deux interpréteurs */}
          <Arrow x1={830} y1={450} x2={560} y2={640} bend={36} color={GREEN} width={4} t={t} delay={9.2} dur={0.8}
            label="F = IO" labelDx={-66} labelDy={4} labelSize={24} />
          <Arrow x1={1090} y1={450} x2={1360} y2={640} bend={-36} color={BLUE} width={4} t={t} delay={9.6} dur={0.8}
            label="F = Test" labelDx={80} labelDy={4} labelSize={24} />

          <Box x={260} y={660} w={580} h={250} label="given KVStore[IO]" color={GREEN} t={t} delay={10.2} labelSize={20} />
          <Cap t={t} from={10.9} x={310} y={720} w={480} align="left" size={24} color={INK}>
            production : Redis,<br></br>vrais effets, vraie latence
          </Cap>
          <Box x={1090} y={660} w={580} h={250} label="given KVStore[Test]" color={BLUE} t={t} delay={10.8} labelSize={20} />
          <Cap t={t} from={11.5} x={1140} y={720} w={500} align="left" size={24} color={INK}>
            tests : une Map en mémoire,<br></br>instantané, déterministe
          </Cap>

          <Cap t={t} from={13.6} to={tCode} x={460} y={990} w={1000} size={30} color={INK}>
            <b>même programme</b>, deux mondes — on ne change que le <span style={{ fontFamily: MONO, color: AMBER, fontWeight: 600 }}>given</span>
          </Cap>
        </div>
      )}
    </SceneShell>
  );
}

// ── 12 · Free ───────────────────────────────────────────────────────────────
const CODE_FREE = [
  'enum Free[F[_], A]:',
  '  case Pure(a: A)',
  '  case Suspend(fa: F[A])',
  '  case Bind[F[_], X, A](fx: Free[F, X],',
  '       f: X => Free[F, A]) extends Free[F, A]',
  '',
  '  def flatMap[B](f: A => Free[F, B]) = Bind(this, f)',
  '',
  { s: '  def foldMap[G[_]: Monad](nt: F ~> G): G[A] =', hl: AMBER },
  '    this match',
  '      case Pure(a)     => summon[Monad[G]].pure(a)',
  { s: '      case Suspend(fa) => nt(fa)', hl: AMBER },
  '      case Bind(fx, f) => fx.foldMap(nt)',
  '            .flatMap(x => f(x).foldMap(nt))',
];

function InstrCard({ x, y, t, delay, text, color = AMBER, lit = false }) {
  const { op, sc } = pop(t, delay);
  if (op <= 0) return null;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: 430, height: 86, opacity: op,
      transform: `scale(${sc})`, background: lit ? 'color-mix(in oklab, #a8742a 14%, #fcfaf4)' : CARD,
      border: `2.5px solid ${color}`, borderRadius: 10,
      display: 'flex', alignItems: 'center', padding: '0 24px',
      fontFamily: MONO, fontSize: 23, fontWeight: 600, color: INK,
      boxShadow: lit ? `0 0 0 4px color-mix(in oklab, ${color} 30%, transparent)` : '3px 4px 0 rgba(43,42,38,0.10)',
    }}>{text}</div>
  );
}

function SceneFree() {
  return (
    <SceneShell ch={CH.free} code={CODE_FREE} codeTitle="Free.scala" codeFontSize={20}
      takeaway="Free = une monade gratuite : le programme est une donnée, qu’on interprète après.">
      {({ t, tCode }) => {
        // l'interpréteur balaie les cartes
        const step = t < 12.5 ? -1 : t < 14.2 ? 0 : t < 15.9 ? 1 : 2;
        return (
          <div style={{ position: 'absolute', inset: 0 }}>
            <Cap t={t} from={3.0} to={7.0} x={460} y={230} w={1000} size={31} color={INK}>
              et si le programme n’était pas <i>exécuté</i>… mais <b>construit</b> ?
            </Cap>

            {/* le programme-donnée : une pile d'instructions */}
            <InstrCard x={290} y={330} t={t} delay={5.0} text='Put("hello", "world")' lit={step === 0} />
            <Arrow x1={505} y1={425} x2={505} y2={485} color={AMBER} width={3} t={t} delay={5.7} dur={0.4}
              label="Bind" labelDx={70} labelDy={10} labelSize={19} />
            <InstrCard x={290} y={500} t={t} delay={6.1} text='Get("hello")' lit={step === 1} />
            <Arrow x1={505} y1={595} x2={505} y2={655} color={AMBER} width={3} t={t} delay={6.8} dur={0.4}
              label="Bind" labelDx={70} labelDy={10} labelSize={19} />
            <InstrCard x={290} y={670} t={t} delay={7.2} text="Pure(())" lit={step === 2} />
            <Box x={240} y={290} w={530} h={520} label="Free[KV, Unit]" color={AMBER} t={t} delay={8.2} dashed={true} labelSize={21} fill="transparent" />
            <Cap t={t} from={9.0} to={12.2} x={210} y={870} w={620} size={28} italic={true} color={INK2}>
              une structure de données — rien n’a encore été exécuté
            </Cap>

            {/* l'interpréteur */}
            <Arrow x1={790} y1={545} x2={1010} y2={545} bend={-30} color={GREEN} width={4.5} t={t} delay={12.0} dur={0.8}
              label="foldMap(KV ~> IO)" labelDy={-32} labelSize={23} />
            <Box x={1040} y={360} w={560} h={380} label="IO — exécution" color={GREEN} t={t} delay={12.6} labelSize={20} />
            <Cap t={t} from={13.6} x={1090} y={425} w={480} align="left" size={23} font={MONO} weight={500} color={step >= 0 ? INK : INK3}>
              redis.set(hello, world)
            </Cap>
            <Cap t={t} from={15.3} x={1090} y={490} w={480} align="left" size={23} font={MONO} weight={500} color={step >= 1 ? INK : INK3}>
              redis.get(hello)
            </Cap>
            <Cap t={t} from={17.0} x={1090} y={555} w={480} align="left" size={23} font={MONO} weight={500} color={step >= 2 ? GREEN : INK3}>
              ✓ terminé
            </Cap>
            <Cap t={t} from={18.2} to={tCode} x={1010} y={790} w={640} size={27} color={INK}>
              l’interpréteur est un <span style={{ fontFamily: MONO, color: AMBER, fontWeight: 600 }}>F ~&gt; G</span> —
              remplacez-le : tests, logs, dry-run…
            </Cap>
          </div>
        );
      }}
    </SceneShell>
  );
}

// ── 13 · Yoneda / Coyoneda ──────────────────────────────────────────────────
const CODE_YONEDA = [
  '// Coyoneda : accumule les map sans les exécuter',
  'final case class Coyoneda[F[_], A, B](',
  '    fb: F[B], f: B => A):',
  '',
  '  def map[C](g: A => C): Coyoneda[F, C, B] =',
  { s: '    Coyoneda(fb, f andThen g)     // fusion !', hl: GREEN },
  '',
  '  def run(using Functor[F]): F[A] = fb.map(f)',
  '',
  'Coyoneda(hugeList, identity)',
  '  .map(_ + 1).map(_ * 2).map(_.toString)',
  { s: '  .run   // UNE seule traversée', hl: GREEN },
];

function SceneYoneda() {
  return (
    <SceneShell ch={CH.yoneda} code={CODE_YONEDA} codeTitle="Coyoneda.scala" codeFontSize={20.5}
      takeaway="Yoneda fusionne les map ; Coyoneda offre un Functor gratuit à n’importe quel F.">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          {/* le problème : 3 traversées */}
          <Box x={250} y={290} w={300} h={170} label="F[A]" color={BLUE} t={t} delay={3.0} labelSize={19} />
          <Arrow x1={570} y1={375} x2={720} y2={375} color={RED} width={3.5} t={t} delay={3.8} label="map(f)" labelDy={-22} labelSize={20} />
          <Box x={740} y={290} w={300} h={170} label="F[B]" color={BLUE} t={t} delay={4.4} labelSize={19} />
          <Arrow x1={1060} y1={375} x2={1210} y2={375} color={RED} width={3.5} t={t} delay={5.0} label="map(g)" labelDy={-22} labelSize={20} />
          <Box x={1230} y={290} w={300} h={170} label="F[C]" color={BLUE} t={t} delay={5.6} labelSize={19} />
          <Arrow x1={1550} y1={375} x2={1700} y2={375} color={RED} width={3.5} t={t} delay={6.2} label="map(h)" labelDy={-22} labelSize={20} />
          <Cap t={t} from={7.2} to={11.0} x={460} y={530} w={1000} size={31} color={INK}>
            trois <span style={{ fontFamily: MONO, color: RED }}>map</span> = <b>trois traversées</b> de la structure
          </Cap>

          {/* la fusion */}
          <Cap t={t} from={11.4} to={14.4} x={460} y={640} w={1000} size={30} italic={true} color={INK2}>
            or les fonctions, elles, se composent <b>gratuitement</b>…
          </Cap>
          <Box x={250} y={750} w={300} h={170} label="F[A]" color={BLUE} t={t} delay={14.2} labelSize={19} />
          <Arrow x1={570} y1={835} x2={1310} y2={835} bend={-44} color={GREEN} width={4.5} t={t} delay={15.0} dur={1.0}
            label="map(f andThen g andThen h)" labelDy={-34} labelSize={24} />
          <Box x={1330} y={750} w={300} h={170} label="F[D]" color={GREEN} t={t} delay={16.0} labelSize={19} />
          <Tag t={t} from={16.8} to={tCode + 1} x={620} y={930} w={640} size={24} color={GREEN}>
            une seule traversée — même résultat
          </Tag>
          <Cap t={t} from={18.0} to={tCode} x={410} y={1010} w={1100} size={27} color={INK}>
            <span style={{ fontFamily: MONO, color: GREEN, fontWeight: 600 }}>Coyoneda</span> fait
            cette comptabilité pour vous — et donne un Functor à tout <span style={{ fontFamily: MONO }}>F[_]</span>
          </Cap>
        </div>
      )}
    </SceneShell>
  );
}

// ── La carte finale ─────────────────────────────────────────────────────────
const MAP_NODES = [
  { id: 'fu', x: 860,  y: 280,  l: 'Functor',     c: BLUE,  d: 2.0 },
  { id: 'ap', x: 860,  y: 450,  l: 'Applicative', c: BLUE,  d: 2.5 },
  { id: 'mo', x: 860,  y: 620,  l: 'Monad',       c: BLUE,  d: 3.0 },
  { id: 'va', x: 430,  y: 380,  l: 'Validated',   c: RED,   d: 5.2 },
  { id: 'tr', x: 400,  y: 540,  l: 'Traverse',    c: GREEN, d: 5.7 },
  { id: 'ei', x: 430,  y: 720,  l: 'Either',      c: RED,   d: 6.2 },
  { id: 'st', x: 640,  y: 850,  l: 'State',       c: GREEN, d: 6.7 },
  { id: 'kl', x: 990,  y: 850,  l: 'Kleisli',     c: GREEN, d: 7.2 },
  { id: 're', x: 1260, y: 950,  l: 'ReaderT',     c: GREEN, d: 7.7 },
  { id: 'yo', x: 1400, y: 280,  l: 'Yoneda · Coyoneda', c: AMBER, d: 9.0 },
  { id: 'fk', x: 1450, y: 480,  l: 'F ~> G',      c: AMBER, d: 9.5 },
  { id: 'fr', x: 1430, y: 660,  l: 'Free',        c: AMBER, d: 10.0 },
  { id: 'tf', x: 1660, y: 800,  l: 'Tagless Final', c: AMBER, d: 10.5 },
];
const MAP_EDGES = [
  { a: 'fu', b: 'ap', l: 'pure · map2', d: 3.8 },
  { a: 'ap', b: 'mo', l: 'flatMap', d: 4.3 },
  { a: 'ap', b: 'va', l: 'accumule', d: 8.0 },
  { a: 'ap', b: 'tr', l: '', d: 8.3 },
  { a: 'mo', b: 'ei', l: 'court-circuit', d: 8.6 },
  { a: 'mo', b: 'st', l: '', d: 8.9 },
  { a: 'mo', b: 'kl', l: '>=>', d: 9.2 },
  { a: 'kl', b: 're', l: '', d: 9.7 },
  { a: 'yo', b: 'fu', l: 'Functor gratuit', d: 11.2 },
  { a: 'fk', b: 'fr', l: 'interprète', d: 11.6 },
  { a: 'fk', b: 'tf', l: 'change de F', d: 12.0 },
  { a: 'mo', b: 'tf', l: '', d: 12.4 },
];

function SceneFinale() {
  const { localTime: t, duration: D } = useSprite();
  const fadeOut = animate({ from: 1, to: 0, start: D - 1.0, end: D - 0.1 })(t);
  const byId = Object.fromEntries(MAP_NODES.map((n) => [n.id, n]));
  const mapOp = animate({ from: 1, to: 0, start: 31, end: 33 })(t);
  const drift = 1 + 0.035 * Math.min(t / 30, 1);
  return (
    <div style={{ position: 'absolute', inset: 0, opacity: fadeOut }}>
      <Cap t={t} from={0.4} to={32} x={60} y={44} w={900} align="left" size={38} font={SERIF} weight={600} color={INK}>
        La carte — tout s’emboîte
      </Cap>

      {mapOp > 0 && (
        <div style={{ position: 'absolute', inset: 0, opacity: mapOp, transform: `scale(${drift})`, transformOrigin: '50% 45%' }}>
          {MAP_EDGES.map((e, i) => {
            const A = byId[e.a], B = byId[e.b];
            return <Arrow key={i} x1={A.x} y1={A.y} x2={B.x} y2={B.y} bend={14} color={INK2} width={2.5}
              t={t} delay={e.d} dur={0.7} label={e.l || undefined} labelSize={19} labelColor={INK2} labelDy={-12} />;
          })}
          {MAP_NODES.map((n, i) => {
            const { op, sc } = pop(t, n.d);
            if (op <= 0) return null;
            const dy = 4 * Math.sin(t * 0.9 + i * 1.4);
            return (
              <div key={n.id} style={{
                position: 'absolute', left: n.x, top: n.y + dy, transform: `translate(-50%,-50%) scale(${sc})`, opacity: op,
                background: CARD, border: `3px solid ${n.c}`, borderRadius: 999,
                padding: '12px 30px', fontFamily: MONO, fontSize: 27, fontWeight: 600, color: n.c,
                boxShadow: '3px 4px 0 rgba(43,42,38,0.12)', whiteSpace: 'nowrap',
              }}>{n.l}</div>
            );
          })}
        </div>
      )}

      <Cap t={t} from={14.5} to={19.5} x={310} y={1000} w={1300} size={30} italic={true} color={INK2}>
        la tour des typeclasses au centre — les erreurs à gauche, la composition en bas, l’interprétation à droite
      </Cap>
      <Cap t={t} from={20.5} to={31} x={310} y={1000} w={1300} size={30} italic={true} color={INK}>
        chaque pattern répond à une seule question : <b>comment composer ceci avec cela ?</b>
      </Cap>

      {/* mot de la fin */}
      <Cap t={t} from={33.5} x={310} y={430} w={1300} size={88} font={SERIF} weight={600} color={INK}>
        Tout n’est que composition.
      </Cap>
      <svg width="560" height="16" style={{ position: 'absolute', left: 680, top: 570, overflow: 'visible' }}>
        <path d="M 4 9 Q 280 2 556 8" fill="none" stroke={BLUE} strokeWidth="3.5" strokeLinecap="round"
          pathLength="1" strokeDasharray="1 1"
          strokeDashoffset={1 - animate({ from: 0, to: 1, start: 34.8, end: 35.8, ease: Easing.easeInOutCubic })(t)}></path>
      </svg>
      <Cap t={t} from={37} x={560} y={640} w={800} size={26} font={MONO} color={INK2}>
        — Scala 3 · les patterns fonctionnels —
      </Cap>
    </div>
  );
}

Object.assign(window, { SceneTagless, SceneFree, SceneYoneda, SceneFinale });
