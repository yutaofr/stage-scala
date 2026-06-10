// fp-act1.jsx — Ouverture, Functor, Applicative, Monad

// ── Constellation de l'ouverture ────────────────────────────────────────────
const INTRO_NODES = [
  { id: 'fu', x: 360,  y: 500,  l: 'Functor',        c: BLUE },
  { id: 'ap', x: 660,  y: 610,  l: 'Applicative',    c: BLUE },
  { id: 'mo', x: 980,  y: 520,  l: 'Monad',          c: BLUE },
  { id: 'ei', x: 1300, y: 590,  l: 'Either',         c: RED },
  { id: 'va', x: 1590, y: 500,  l: 'Validated',      c: RED },
  { id: 'tr', x: 470,  y: 750,  l: 'Traverse',       c: GREEN },
  { id: 'st', x: 770,  y: 830,  l: 'State',          c: GREEN },
  { id: 'kl', x: 1080, y: 740,  l: 'Kleisli',        c: GREEN },
  { id: 're', x: 1370, y: 810,  l: 'ReaderT',        c: GREEN },
  { id: 'fk', x: 1650, y: 720,  l: 'F ~> G',         c: AMBER },
  { id: 'fr', x: 900,  y: 950,  l: 'Free',           c: AMBER },
  { id: 'tf', x: 1210, y: 930,  l: 'Tagless Final',  c: AMBER },
  { id: 'yo', x: 1520, y: 950,  l: 'Yoneda',         c: AMBER },
];
const INTRO_EDGES = [['fu','ap'],['ap','mo'],['mo','ei'],['ei','va'],['ap','tr'],['mo','st'],['mo','kl'],['kl','re'],['fk','fr'],['fk','tf'],['yo','fu']];

function SceneIntro() {
  const { localTime: t, duration: D } = useSprite();
  const fadeOut = animate({ from: 1, to: 0, start: D - 0.8, end: D - 0.05 })(t);
  const nodeById = Object.fromEntries(INTRO_NODES.map((n) => [n.id, n]));
  return (
    <div style={{ position: 'absolute', inset: 0, opacity: fadeOut }}>
      <Cap t={t} from={0.4} x={560} y={130} w={800} size={26} font={MONO} color={INK2} weight={500}>
        — Scala 3 —
      </Cap>
      <Cap t={t} from={1.0} x={260} y={185} w={1400} size={84} font={SERIF} weight={600} color={INK}>
        Les patterns de la<br></br>programmation fonctionnelle
      </Cap>
      <svg width="520" height="16" style={{ position: 'absolute', left: 700, top: 430, overflow: 'visible' }}>
        <path d="M 4 9 Q 260 2 516 8" fill="none" stroke={INK} strokeWidth="3" strokeLinecap="round"
          pathLength="1" strokeDasharray="1 1"
          strokeDashoffset={1 - animate({ from: 0, to: 1, start: 2.4, end: 3.3, ease: Easing.easeInOutCubic })(t)}></path>
      </svg>

      {/* arêtes */}
      {INTRO_EDGES.map(([a, b], i) => {
        const A = nodeById[a], B = nodeById[b];
        return <Arrow key={i} x1={A.x} y1={A.y} x2={B.x} y2={B.y} bend={18} color={INK3}
          width={2} t={t} delay={9.5 + i * 0.22} dur={0.8} dashed={true} />;
      })}
      {/* nœuds */}
      {INTRO_NODES.map((n, i) => {
        const { op, sc } = pop(t, 5.2 + i * 0.32);
        if (op <= 0) return null;
        const dy = 4 * Math.sin(t * 1.1 + i * 1.7);
        return (
          <div key={n.id} style={{
            position: 'absolute', left: n.x, top: n.y + dy, transform: `translate(-50%,-50%) scale(${sc})`, opacity: op,
            background: CARD, border: `2.5px solid ${n.c}`, borderRadius: 999,
            padding: '10px 26px', fontFamily: MONO, fontSize: 25, fontWeight: 600, color: n.c,
            boxShadow: '3px 4px 0 rgba(43,42,38,0.10)', whiteSpace: 'nowrap',
          }}>{n.l}</div>
        );
      })}

      <Cap t={t} from={13.2} to={17.2} x={460} y={1000} w={1000} size={34} italic={true} color={INK}>
        Treize patterns. Une seule idée : <b>la composition</b>.
      </Cap>
      <Cap t={t} from={17.6} x={460} y={1000} w={1000} size={34} italic={true} color={INK2}>
        L’intuition d’abord — le code ensuite.
      </Cap>
    </div>
  );
}

// ── 01 · Functor ────────────────────────────────────────────────────────────
const CODE_FUNCTOR = [
  'trait Functor[F[_]]:',
  '  extension [A](fa: F[A])',
  { s: '    def map[B](f: A => B): F[B]', hl: BLUE },
  '',
  'given Functor[Option] with',
  '  extension [A](fa: Option[A])',
  '    def map[B](f: A => B): Option[B] =',
  '      fa match',
  '        case Some(a) => Some(f(a))',
  '        case None    => None',
  '',
  'Some(21).map(_ * 2)   // Some(42)',
  'None.map(_ * 2)       // None',
];

function SceneFunctor() {
  return (
    <SceneShell ch={CH.functor} code={CODE_FUNCTOR} codeTitle="Functor.scala"
      takeaway="map transforme le contenu, jamais la structure.">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          {/* la fonction nue, au-dessus */}
          <Token x={560} y={250} r={40} label="21" color={INK} t={t} delay={5.8} />
          <Arrow x1={620} y1={250} x2={920} y2={250} bend={-26} color={INK} t={t} delay={6.4}
            label="f = _ * 2" labelDy={-24} />
          <Token x={985} y={250} r={40} label="42" color={INK} t={t} delay={7.1} />
          <Tag t={t} from={7.6} x={1100} y={236} w={460} align="left" size={22}>une simple fonction A =&gt; B</Tag>

          {/* la boîte F[A] */}
          <Box x={330} y={460} w={380} h={300} label="F[A]" color={BLUE} t={t} delay={3.0} />
          <Token x={520} y={615} r={46} label="21" color={INK} t={t} delay={3.6} fontSize={30} />
          <Cap t={t} from={4.2} to={8.6} x={210} y={820} w={620} size={28} italic={true} color={INK2}>
            une valeur, prisonnière d’un contexte
          </Cap>

          {/* la question */}
          <Cap t={t} from={9.0} to={12.6} x={560} y={950} w={800} size={34} color={INK}>
            Comment appliquer <span style={{ fontFamily: MONO, color: BLUE }}>f</span> sans ouvrir la boîte ?
          </Cap>

          {/* map : la flèche relevée */}
          <Arrow x1={730} y1={610} x2={1180} y2={610} bend={-60} color={BLUE} width={4} t={t} delay={12.8} dur={0.9}
            label="map(f)" labelDy={-30} labelSize={27} />
          <Box x={1200} y={460} w={380} h={300} label="F[B]" color={BLUE} t={t} delay={13.6} />
          <Token x={1390} y={615} r={46} label="42" color={INK} t={t} delay={14.2} fontSize={30} />

          <Cap t={t} from={15.4} to={tCode} x={460} y={950} w={1000} size={32} color={INK}>
            <span style={{ fontFamily: MONO, color: BLUE, fontWeight: 600 }}>map</span> fait
            entrer la fonction <i>à l’intérieur</i> du contexte — la boîte reste intacte.
          </Cap>

          {/* lois, en phase code */}
          <Tag t={t} from={tCode + 7} x={140} y={1280} w={900} align="left" size={26} color={INK2}>
            lois — fa.map(identity) == fa
          </Tag>
          <Tag t={t} from={tCode + 8} x={140} y={1330} w={1000} align="left" size={26} color={INK2}>
            {'        fa.map(f).map(g) == fa.map(f andThen g)'}
          </Tag>
        </div>
      )}
    </SceneShell>
  );
}

// ── 02 · Applicative ────────────────────────────────────────────────────────
const CODE_APPLIC = [
  'trait Applicative[F[_]] extends Functor[F]:',
  { s: '  def pure[A](a: A): F[A]', hl: GREEN },
  '  extension [A](fa: F[A])',
  { s: '    def map2[B, C](fb: F[B])(f: (A, B) => C): F[C]', hl: BLUE },
  '',
  'case class User(name: String, age: Int)',
  '',
  'val name: Option[String] = Some("Ada")',
  'val age : Option[Int]    = Some(36)',
  '',
  'name.map2(age)(User.apply)',
  '// Some(User("Ada", 36)) — aucun ordre requis',
];

function SceneApplic() {
  return (
    <SceneShell ch={CH.applic} code={CODE_APPLIC} codeTitle="Applicative.scala" codeFontSize={21}
      takeaway="Effets indépendants ⇒ on les combine, sans imposer d’ordre.">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          {/* deux effets indépendants */}
          <Box x={290} y={300} w={400} h={260} label="F[String]" color={BLUE} t={t} delay={3.0} />
          <Token x={490} y={435} r={56} label='"Ada"' color={INK} t={t} delay={3.6} fontSize={26} />
          <Box x={830} y={300} w={400} h={260} label="F[Int]" color={BLUE} t={t} delay={3.8} />
          <Token x={1030} y={435} r={56} label="36" color={INK} t={t} delay={4.4} fontSize={28} />
          <Cap t={t} from={5.2} to={9.2} x={360} y={210} w={800} size={30} italic={true} color={INK2}>
            deux effets <b>indépendants</b> — aucun n’attend l’autre
          </Cap>

          {/* map ne suffit pas */}
          <Cap t={t} from={9.6} to={12.8} x={310} y={640} w={900} size={32} color={INK}>
            <span style={{ fontFamily: MONO, color: BLUE }}>map</span> ne voit qu’une boîte à la fois…
          </Cap>

          {/* convergence map2 */}
          <Arrow x1={490} y1={575} x2={700} y2={800} bend={40} color={GREEN} width={4} t={t} delay={13.2} dur={0.8} />
          <Arrow x1={1030} y1={575} x2={820} y2={800} bend={-40} color={GREEN} width={4} t={t} delay={13.5} dur={0.8}
            label="map2(f)" labelDx={120} labelDy={10} labelSize={27} />
          <Box x={530} y={810} w={460} h={230} label="F[User]" color={GREEN} t={t} delay={14.4} />
          <Cap t={t} from={15.0} x={555} y={895} w={410} size={30} font={MONO} weight={600} color={INK}>
            User("Ada", 36)
          </Cap>

          {/* pure */}
          <Token x={1530} y={330} r={36} label="a" color={INK} t={t} delay={16.6} />
          <Arrow x1={1530} y1={385} x2={1530} y2={500} color={GREEN} width={3.5} t={t} delay={17.2}
            label="pure" labelDx={66} labelDy={8} />
          <Box x={1400} y={520} w={260} h={170} label="F[A]" color={GREEN} t={t} delay={17.9} />
          <Token x={1530} y={612} r={34} label="a" color={INK} t={t} delay={18.4} />
          <Tag t={t} from={18.8} to={tCode} x={1330} y={730} w={400} size={21}>pure : entrer dans le contexte</Tag>
        </div>
      )}
    </SceneShell>
  );
}

// ── 03 · Monad ──────────────────────────────────────────────────────────────
const CODE_MONAD = [
  'trait Monad[F[_]] extends Applicative[F]:',
  '  extension [A](fa: F[A])',
  { s: '    def flatMap[B](f: A => F[B]): F[B]', hl: BLUE },
  '',
  'def user(id: Int):     Option[User]',
  'def account(u: User):  Option[Account]',
  '',
  'val balance: Option[BigDecimal] =',
  '  for',
  { s: '    u   <- user(42)      // étape 1', hl: GREEN },
  { s: '    acc <- account(u)    // dépend de u !', hl: GREEN },
  '  yield acc.balance',
];

function SceneMonad() {
  return (
    <SceneShell ch={CH.monad} code={CODE_MONAD} codeTitle="Monad.scala"
      takeaway="Monad = la séquence : chaque étape dépend du résultat de la précédente.">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          {/* le problème : f renvoie une boîte */}
          <Box x={250} y={300} w={330} h={240} label="F[A]" color={BLUE} t={t} delay={3.0} />
          <Token x={415} y={425} r={42} label="42" color={INK} t={t} delay={3.5} fontSize={26} />
          <Arrow x1={590} y1={420} x2={840} y2={420} bend={-20} color={INK} t={t} delay={4.6}
            label="f: A => F[B]" labelDy={-24} />
          <Cap t={t} from={5.4} to={9.0} x={250} y={620} w={760} size={30} italic={true} color={INK2}>
            l’étape suivante renvoie <b>elle aussi</b> une boîte…
          </Cap>

          {/* la boîte imbriquée */}
          <Box x={870} y={250} w={430} h={350} label="F[F[B]]" color={RED} t={t} delay={6.4} dashed={true} />
          <Box x={960} y={340} w={250} h={190} label="F[B]" color={BLUE} t={t} delay={6.9} labelSize={19} />
          <Token x={1085} y={440} r={36} label="b" color={INK} t={t} delay={7.4} />
          <Cap t={t} from={9.4} to={12.4} x={830} y={660} w={520} size={32} color={RED} weight={600}>
            le piège : F[F[B]]
          </Cap>

          {/* flatten */}
          <Arrow x1={1320} y1={420} x2={1530} y2={420} bend={-20} color={GREEN} width={4} t={t} delay={12.6}
            label="flatten" labelDy={-26} labelSize={25} />
          <Box x={1550} y={310} w={300} h={220} label="F[B]" color={GREEN} t={t} delay={13.4} />
          <Token x={1700} y={425} r={38} label="b" color={INK} t={t} delay={13.9} />
          <Cap t={t} from={14.8} to={17.8} x={560} y={730} w={800} size={32} color={INK}>
            <span style={{ fontFamily: MONO, color: BLUE, fontWeight: 600 }}>flatMap</span> = map, puis flatten.
          </Cap>

          {/* la chaîne séquentielle */}
          <Box x={250} y={830} w={300} h={170} label="user(42)" color={BLUE} t={t} delay={18.2} labelSize={19} />
          <Arrow x1={570} y1={915} x2={760} y2={915} color={BLUE} width={3.5} t={t} delay={18.9} label="flatMap" labelDy={-20} labelSize={20} />
          <Box x={780} y={830} w={320} h={170} label="account(u)" color={BLUE} t={t} delay={19.5} labelSize={19} />
          <Arrow x1={1120} y1={915} x2={1310} y2={915} color={BLUE} width={3.5} t={t} delay={20.2} label="flatMap" labelDy={-20} labelSize={20} />
          <Box x={1330} y={830} w={300} h={170} label="balance" color={GREEN} t={t} delay={20.8} labelSize={19} />
          <Tag t={t} from={21.4} to={tCode + 0.5} x={510} y={1040} w={900} size={23}>
            une chaîne — chaque maillon dépend du précédent
          </Tag>

          {/* lois en phase code */}
          <Tag t={t} from={tCode + 8} x={140} y={1280} w={1100} align="left" size={25} color={INK2}>
            lois — pure(a).flatMap(f) == f(a)   ·   fa.flatMap(pure) == fa
          </Tag>
        </div>
      )}
    </SceneShell>
  );
}

Object.assign(window, { SceneIntro, SceneFunctor, SceneApplic, SceneMonad });
