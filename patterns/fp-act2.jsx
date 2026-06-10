// fp-act2.jsx — Either, Validated, Traverse, State

// ── 04 · Either ─────────────────────────────────────────────────────────────
const CODE_EITHER = [
  'enum ApiError:',
  '  case Invalid(msg: String)',
  '  case NotFound',
  '',
  'def parse(raw: String): Either[ApiError, Int]',
  'def fetch(id: Int):     Either[ApiError, User]',
  '',
  'val res: Either[ApiError, User] =',
  '  for',
  { s: '    id <- parse("42")   // Left ? on s’arrête', hl: RED },
  { s: '    u  <- fetch(id)     // sauté si Left', hl: RED },
  '  yield u',
];

function MovingDot({ x, y, color = INK, label, r = 26 }) {
  return (
    <div style={{
      position: 'absolute', left: x - r, top: y - r, width: r * 2, height: r * 2,
      borderRadius: r, border: `3px solid ${color}`, background: '#fff',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: MONO, fontSize: 20, fontWeight: 600, color,
      boxShadow: '2px 3px 0 rgba(43,42,38,0.15)',
    }}>{label}</div>
  );
}

function SceneEither() {
  return (
    <SceneShell ch={CH.either} code={CODE_EITHER} codeTitle="Either.scala"
      takeaway="Either court-circuite : après un Left, plus rien ne s’exécute.">
      {({ t, tCode }) => {
        // voie verte (Right) y=430, voie rouge (Left) y=680
        const happy = clamp((t - 5.0) / 4.0, 0, 1);
        const hx = 200 + happy * 1450;
        // run 2 : échec à la station fetch
        const f = clamp((t - 11.5) / 4.5, 0, 1);
        const fx = 200 + f * 1450;
        const fy = f < 0.42 ? 430 : f < 0.55 ? 430 + ((f - 0.42) / 0.13) * 250 : 680;
        return (
          <div style={{ position: 'absolute', inset: 0 }}>
            {/* rails */}
            <Arrow x1={170} y1={430} x2={1750} y2={430} color={GREEN} width={4} t={t} delay={3.0} dur={1.1} />
            <Arrow x1={170} y1={680} x2={1750} y2={680} color={RED} width={4} t={t} delay={3.4} dur={1.1} />
            <Tag t={t} from={4.2} x={200} y={300} w={500} align="left" size={23} color={GREEN}>Right — la voie nominale</Tag>
            <Tag t={t} from={4.5} x={200} y={720} w={500} align="left" size={23} color={RED}>Left — la voie d’erreur</Tag>

            {/* stations */}
            <Box x={480} y={365} w={250} h={130} label="parse" color={INK} t={t} delay={4.0} labelSize={20} />
            <Box x={930} y={365} w={250} h={130} label="fetch" color={INK} t={t} delay={4.2} labelSize={20} />
            <Box x={1380} y={365} w={250} h={130} label="yield" color={INK} t={t} delay={4.4} labelSize={20} />

            {/* trajet nominal */}
            {t >= 5.0 && t < 10.8 && <MovingDot x={hx} y={430} color={GREEN} label="42" />}
            {happy >= 1 && <Mark x={1790} y={430} kind="check" t={t} delay={9.2} size={44} />}
            <Cap t={t} from={9.4} to={11.2} x={1100} y={250} w={700} size={28} italic={true} color={INK2}>
              tout va bien : on reste sur la voie verte
            </Cap>

            {/* trajet en échec */}
            {t >= 11.5 && <MovingDot x={fx} y={fy} color={f >= 0.42 ? RED : GREEN} label={f >= 0.42 ? 'Left' : '"a"'} r={30} />}
            {f >= 0.42 && <Arrow x1={1055} y1={460} x2={1130} y2={655} bend={30} color={RED} width={3} t={t} delay={13.4} dur={0.5} />}
            {f >= 1 && <Mark x={1790} y={680} kind="cross" t={t} delay={15.8} size={44} />}
            <Cap t={t} from={13.8} to={tCode} x={510} y={840} w={950} size={31} color={INK}>
              un échec <b>aiguille</b> vers la voie rouge — <span style={{ fontFamily: MONO, color: INK2 }}>yield</span> ne
              sera jamais atteint
            </Cap>
          </div>
        );
      }}
    </SceneShell>
  );
}

// ── 05 · Validated ──────────────────────────────────────────────────────────
const CODE_VALIDATED = [
  'enum Validated[+E, +A]:',
  '  case Valid(a: A)',
  '  case Invalid(errors: List[E])',
  '',
  '  def zip[EE >: E, B](that: Validated[EE, B]) =',
  '    (this, that) match',
  '      case (Valid(a), Valid(b)) => Valid((a, b))',
  { s: '      case (Invalid(e1), Invalid(e2))', hl: RED },
  { s: '                  => Invalid(e1 ++ e2)  // cumul !', hl: RED },
  '      case (Invalid(e), _) => Invalid(e)',
  '      case (_, Invalid(e)) => Invalid(e)',
  '',
  'checkName(n).zip(checkAge(a)).zip(checkMail(m))',
];

function FieldRow({ x, y, label, value, ok, t, delay }) {
  const { op, sc } = pop(t, delay);
  if (op <= 0) return null;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: 470, height: 100,
      border: `2.5px solid ${INK}`, borderRadius: 12, background: CARD,
      display: 'flex', alignItems: 'center', gap: 18, padding: '0 26px',
      opacity: op, transform: `scale(${sc})`,
      fontFamily: MONO, fontSize: 24,
    }}>
      <span style={{ color: INK2 }}>{label}</span>
      <span style={{ color: INK, fontWeight: 600 }}>{value}</span>
    </div>
  );
}

function SceneValidated() {
  return (
    <SceneShell ch={CH.validated} code={CODE_VALIDATED} codeTitle="Validated.scala" codeFontSize={21}
      takeaway="Validated n’est pas une monade : c’est un Applicative qui accumule.">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          <FieldRow x={230} y={300} label="name " value='"Ada"' t={t} delay={3.0} />
          <FieldRow x={230} y={460} label="age  " value="-5" t={t} delay={3.3} />
          <FieldRow x={230} y={620} label="email" value='"ada@"' t={t} delay={3.6} />
          <Cap t={t} from={4.4} to={8.2} x={230} y={210} w={700} size={28} italic={true} color={INK2}>
            un formulaire, trois validations
          </Cap>

          {/* Either s'arrêterait au premier échec */}
          <Cap t={t} from={8.4} to={12.4} x={230} y={790} w={840} size={30} color={INK}>
            avec <span style={{ fontFamily: MONO, color: RED }}>Either</span> : arrêt à <span style={{ fontFamily: MONO }}>age</span> —
            l’email n’est <b>jamais vérifié</b>
          </Cap>

          {/* trois vérifications en parallèle */}
          <Arrow x1={720} y1={350} x2={920} y2={350} color={GREEN} width={3.5} t={t} delay={12.6} dur={0.5} />
          <Arrow x1={720} y1={510} x2={920} y2={510} color={RED} width={3.5} t={t} delay={12.9} dur={0.5} />
          <Arrow x1={720} y1={670} x2={920} y2={670} color={RED} width={3.5} t={t} delay={13.2} dur={0.5} />
          <Mark x={965} y={350} kind="check" t={t} delay={13.3} size={42} />
          <Mark x={965} y={510} kind="cross" t={t} delay={13.6} size={42} />
          <Mark x={965} y={670} kind="cross" t={t} delay={13.9} size={42} />
          <Tag t={t} from={14.2} to={tCode} x={780} y={240} w={420} size={22} color={GREEN}>tout est vérifié, en parallèle</Tag>

          {/* accumulation */}
          <Arrow x1={1010} y1={510} x2={1190} y2={530} bend={-12} color={RED} width={3.5} t={t} delay={14.8} dur={0.5} />
          <Arrow x1={1010} y1={670} x2={1190} y2={610} bend={18} color={RED} width={3.5} t={t} delay={15.1} dur={0.5} />
          <Box x={1210} y={420} w={580} h={300} label="Invalid" color={RED} t={t} delay={15.6} labelSize={22} />
          <Cap t={t} from={16.2} x={1250} y={480} w={500} align="left" size={25} font={MONO} weight={500} color={INK}>
            List(
          </Cap>
          <Cap t={t} from={16.6} x={1290} y={530} w={500} align="left" size={25} font={MONO} weight={500} color={RED}>
            "âge négatif",
          </Cap>
          <Cap t={t} from={17.0} x={1290} y={580} w={500} align="left" size={25} font={MONO} weight={500} color={RED}>
            "email invalide"
          </Cap>
          <Cap t={t} from={17.3} x={1250} y={630} w={500} align="left" size={25} font={MONO} weight={500} color={INK}>
            )
          </Cap>
          <Cap t={t} from={18.0} to={tCode} x={1190} y={790} w={640} size={28} italic={true} color={INK2}>
            toutes les erreurs, d’un coup
          </Cap>
        </div>
      )}
    </SceneShell>
  );
}

// ── 06 · Traverse ───────────────────────────────────────────────────────────
const CODE_TRAVERSE = [
  'def traverse[F[_]: Applicative, A, B]',
  '    (as: List[A])(f: A => F[B]): F[List[B]] =',
  '  val App = summon[Applicative[F]]',
  '  as.foldRight(App.pure(List.empty[B])):',
  { s: '    (a, acc) => f(a).map2(acc)(_ :: _)', hl: BLUE },
  '',
  'def parseInt(s: String): Option[Int]',
  '',
  'traverse(List("1", "2", "3"))(parseInt)',
  { s: '// Some(List(1, 2, 3))', hl: GREEN },
  'traverse(List("1", "x", "3"))(parseInt)',
  { s: '// None — un seul échec suffit', hl: RED },
];

function SceneTraverse() {
  return (
    <SceneShell ch={CH.traverse} code={CODE_TRAVERSE} codeTitle="Traverse.scala" codeFontSize={21}
      takeaway="traverse retourne la structure : List[F[B]] devient F[List[B]].">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          {/* la liste de départ */}
          <Cap t={t} from={3.0} x={290} y={258} w={120} size={64} font={MONO} color={INK}>[</Cap>
          <Token x={420} y={300} r={38} label='"1"' color={INK} t={t} delay={3.2} />
          <Token x={560} y={300} r={38} label='"2"' color={INK} t={t} delay={3.4} />
          <Token x={700} y={300} r={38} label='"3"' color={INK} t={t} delay={3.6} />
          <Cap t={t} from={3.8} x={760} y={258} w={120} size={64} font={MONO} color={INK}>]</Cap>
          <Tag t={t} from={4.2} x={880} y={290} w={460} align="left" size={23}>List[String]</Tag>

          {/* f sur chaque élément */}
          <Arrow x1={420} y1={355} x2={420} y2={480} color={BLUE} width={3} t={t} delay={5.6} dur={0.5} />
          <Arrow x1={560} y1={355} x2={560} y2={480} color={BLUE} width={3} t={t} delay={5.8} dur={0.5} />
          <Tag t={t} from={6.0} x={770} y={400} w={400} align="left" size={23} color={BLUE}>f: A =&gt; F[B]</Tag>
          <Arrow x1={700} y1={355} x2={700} y2={480} color={BLUE} width={3} t={t} delay={6.0} dur={0.5} />
          <Box x={355} y={500} w={130} h={140} label="F[B]" color={BLUE} t={t} delay={6.4} labelSize={17} />
          <Token x={420} y={575} r={32} label="1" color={INK} t={t} delay={6.7} />
          <Box x={495} y={500} w={130} h={140} label="F[B]" color={BLUE} t={t} delay={6.8} labelSize={17} />
          <Token x={560} y={575} r={32} label="2" color={INK} t={t} delay={7.1} />
          <Box x={635} y={500} w={130} h={140} label="F[B]" color={BLUE} t={t} delay={7.2} labelSize={17} />
          <Token x={700} y={575} r={32} label="3" color={INK} t={t} delay={7.5} />
          <Cap t={t} from={8.4} to={12.2} x={230} y={700} w={760} size={30} color={INK}>
            <span style={{ fontFamily: MONO, color: BLUE }}>List[F[B]]</span> — une liste de boîtes… encombrant.
          </Cap>

          {/* le retournement */}
          <Arrow x1={840} y1={570} x2={1100} y2={570} bend={-40} color={GREEN} width={4.5} t={t} delay={12.6} dur={0.9}
            label="traverse" labelDy={-32} labelSize={26} />
          <Box x={1130} y={470} w={620} h={210} label="F[List[B]]" color={GREEN} t={t} delay={13.6} />
          <Cap t={t} from={14.2} x={1190} y={540} w={120} size={56} font={MONO} color={INK}>[</Cap>
          <Token x={1330} y={575} r={34} label="1" color={INK} t={t} delay={14.4} />
          <Token x={1450} y={575} r={34} label="2" color={INK} t={t} delay={14.6} />
          <Token x={1570} y={575} r={34} label="3" color={INK} t={t} delay={14.8} />
          <Cap t={t} from={15.0} x={1620} y={540} w={120} size={56} font={MONO} color={INK}>]</Cap>
          <Cap t={t} from={15.6} to={tCode} x={1110} y={720} w={680} size={28} italic={true} color={INK2}>
            une <b>seule</b> boîte, contenant la liste
          </Cap>

          {/* l'échec */}
          <Token x={420} y={920} r={32} label="1" color={INK} t={t} delay={17.0} />
          <Token x={540} y={920} r={32} label='"x"' color={RED} t={t} delay={17.2} />
          <Token x={660} y={920} r={32} label="3" color={INK} t={t} delay={17.4} />
          <Arrow x1={730} y1={920} x2={930} y2={920} color={RED} width={3.5} t={t} delay={17.8} dur={0.5} />
          <Cap t={t} from={18.3} to={tCode + 1} x={950} y={895} w={400} align="left" size={30} font={MONO} weight={600} color={RED}>
            None
          </Cap>
          <Tag t={t} from={18.8} to={tCode + 1} x={300} y={1000} w={800} align="left" size={23} color={INK2}>
            un seul échec ⇒ tout le résultat échoue
          </Tag>
        </div>
      )}
    </SceneShell>
  );
}

// ── 07 · State ──────────────────────────────────────────────────────────────
const CODE_STATE = [
  'opaque type State[S, A] = S => (S, A)',
  '',
  'extension [S, A](sa: State[S, A])',
  '  def run(s: S): (S, A) = sa(s)',
  '  def flatMap[B](f: A => State[S, B]) =',
  '    (s0: S) =>',
  { s: '      val (s1, a) = sa(s0)   // fil cousu', hl: GREEN },
  { s: '      f(a)(s1)               // automatiquement', hl: GREEN },
  '',
  'val next: State[Int, Int] = s => (s + 1, s)',
  '',
  'val pair = for x <- next; y <- next yield (x, y)',
  'pair.run(0)   // (2, (0, 1))',
];

function MachineBox({ x, y, w = 300, h = 180, label, t, delay, color = GREEN }) {
  const { op, sc } = pop(t, delay);
  if (op <= 0) return null;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: w, height: h,
      border: `2.5px solid ${color}`, borderRadius: 14, background: CARD,
      opacity: op, transform: `scale(${sc})`,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: MONO, fontSize: 25, fontWeight: 600, color,
      boxShadow: '4px 5px 0 rgba(43,42,38,0.10)',
    }}>
      {label}
      <div style={{ position: 'absolute', inset: 8, border: `1.5px dashed color-mix(in oklab, ${color} 40%, transparent)`, borderRadius: 9 }}></div>
    </div>
  );
}

function SceneState() {
  return (
    <SceneShell ch={CH.state} code={CODE_STATE} codeTitle="State.scala" codeFontSize={21}
      takeaway="L’état devient une valeur — pur, testable, composable.">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          {/* la machine */}
          <MachineBox x={680} y={330} w={420} h={220} label="S => (S, A)" t={t} delay={3.0} />
          <Token x={500} y={440} r={36} label="s" color={INK} t={t} delay={4.4} />
          <Arrow x1={550} y1={440} x2={665} y2={440} color={INK} width={3.5} t={t} delay={4.9} dur={0.5} />
          <Arrow x1={1110} y1={395} x2={1270} y2={330} bend={-16} color={BLUE} width={3.5} t={t} delay={6.0} dur={0.6} />
          <Token x={1330} y={310} r={36} label="a" color={BLUE} t={t} delay={6.6} />
          <Tag t={t} from={7.0} x={1410} y={296} w={420} align="left" size={22} color={BLUE}>la valeur produite</Tag>
          <Arrow x1={1110} y1={490} x2={1270} y2={550} bend={16} color={GREEN} width={3.5} t={t} delay={7.2} dur={0.6} />
          <Token x={1330} y={575} r={36} label="s′" color={GREEN} t={t} delay={7.8} />
          <Tag t={t} from={8.2} x={1410} y={561} w={420} align="left" size={22} color={GREEN}>le nouvel état</Tag>
          <Cap t={t} from={9.2} to={12.6} x={460} y={660} w={900} size={30} italic={true} color={INK2}>
            une fonction pure : l’état entre, un nouvel état ressort
          </Cap>

          {/* l'enchaînement : le fil de l'état */}
          <MachineBox x={290} y={800} w={260} h={150} label="next" t={t} delay={13.0} />
          <MachineBox x={790} y={800} w={260} h={150} label="next" t={t} delay={13.3} />
          <Token x={180} y={875} r={30} label="0" color={GREEN} t={t} delay={13.8} />
          <Arrow x1={560} y1={875} x2={775} y2={875} color={GREEN} width={3.5} t={t} delay={14.4}
            label="s = 1" labelDy={26} labelSize={21} />
          <Arrow x1={1060} y1={875} x2={1240} y2={875} color={GREEN} width={3.5} t={t} delay={15.4}
            label="s = 2" labelDy={26} labelSize={21} />
          <Arrow x1={420} y1={790} x2={420} y2={720} color={BLUE} width={3} t={t} delay={14.9} dur={0.4} />
          <Cap t={t} from={15.2} x={360} y={668} w={200} size={24} font={MONO} weight={600} color={BLUE}>x = 0</Cap>
          <Arrow x1={920} y1={790} x2={920} y2={720} color={BLUE} width={3} t={t} delay={15.9} dur={0.4} />
          <Cap t={t} from={16.2} x={860} y={668} w={200} size={24} font={MONO} weight={600} color={BLUE}>y = 1</Cap>
          <Cap t={t} from={16.8} x={1270} y={848} w={400} align="left" size={26} font={MONO} weight={600} color={INK}>
            (2, (0, 1))
          </Cap>
          <Cap t={t} from={17.8} to={tCode} x={460} y={1010} w={1000} size={29} color={INK}>
            le fil de l’état est cousu par <span style={{ fontFamily: MONO, color: GREEN, fontWeight: 600 }}>flatMap</span> —
            aucune variable mutable
          </Cap>
        </div>
      )}
    </SceneShell>
  );
}

Object.assign(window, { SceneEither, SceneValidated, SceneTraverse, SceneState });
