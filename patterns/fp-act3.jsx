// fp-act3.jsx — Kleisli, ReaderT, FunctionK / Natural Transformation

// ── 08 · Kleisli ────────────────────────────────────────────────────────────
const CODE_KLEISLI = [
  'final case class Kleisli[F[_], A, B](run: A => F[B]):',
  '',
  '  infix def >=>[C](k: Kleisli[F, B, C])',
  '             (using Monad[F]): Kleisli[F, A, C] =',
  { s: '    Kleisli(a => run(a).flatMap(k.run))', hl: BLUE },
  '',
  'val findUser:  Kleisli[Option, Int, User]   // déclarés',
  'val firstCard: Kleisli[Option, User, Card]  // ailleurs',
  '',
  { s: 'val pipeline = findUser >=> firstCard', hl: GREEN },
  'pipeline.run(42)   // Option[Card]',
];

function SceneKleisli() {
  return (
    <SceneShell ch={CH.kleisli} code={CODE_KLEISLI} codeTitle="Kleisli.scala" codeFontSize={21}
      takeaway="Kleisli compose les fonctions A => F[B] comme de simples fonctions.">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          {/* deux fonctions effectful */}
          <Token x={300} y={330} r={40} label="A" color={INK} t={t} delay={3.0} />
          <Arrow x1={360} y1={330} x2={560} y2={330} bend={-18} color={BLUE} width={3.5} t={t} delay={3.5}
            label="f" labelDy={-22} labelSize={24} />
          <Box x={590} y={250} w={280} h={170} label="F[B]" color={BLUE} t={t} delay={4.2} />
          <Token x={730} y={340} r={32} label="b" color={INK} t={t} delay={4.6} />

          <Token x={1050} y={330} r={40} label="B" color={INK} t={t} delay={5.2} />
          <Arrow x1={1110} y1={330} x2={1310} y2={330} bend={-18} color={BLUE} width={3.5} t={t} delay={5.7}
            label="g" labelDy={-22} labelSize={24} />
          <Box x={1340} y={250} w={280} h={170} label="F[C]" color={BLUE} t={t} delay={6.4} />
          <Token x={1480} y={340} r={32} label="c" color={INK} t={t} delay={6.8} />

          {/* le décalage de types */}
          <Arrow x1={880} y1={340} x2={1000} y2={335} bend={20} color={RED} width={3} dashed={true} t={t} delay={8.2} dur={0.7} />
          <Mark x={942} y={385} kind="cross" t={t} delay={9.0} size={40} />
          <Cap t={t} from={9.4} to={13.4} x={460} y={500} w={1000} size={31} color={INK}>
            <span style={{ fontFamily: MONO, color: BLUE }}>F[B]</span> n’est pas
            <span style={{ fontFamily: MONO }}> B</span> —
            <span style={{ fontFamily: MONO, color: INK2 }}> f andThen g</span> ne compile pas
          </Cap>

          {/* la composition Kleisli */}
          <Cap t={t} from={13.8} to={16.6} x={460} y={640} w={1000} size={30} italic={true} color={INK2}>
            on compose <b>dans</b> la catégorie de F, via flatMap…
          </Cap>
          <Token x={350} y={850} r={44} label="A" color={INK} t={t} delay={16.2} fontSize={28} />
          <Arrow x1={420} y1={850} x2={1180} y2={850} bend={-46} color={GREEN} width={5} t={t} delay={16.8} dur={1.1}
            label="f >=> g" labelDy={-34} labelSize={30} />
          <Box x={1210} y={760} w={320} h={190} label="F[C]" color={GREEN} t={t} delay={17.9} />
          <Token x={1370} y={860} r={36} label="c" color={INK} t={t} delay={18.4} />
          <Tag t={t} from={19.0} to={tCode + 1} x={500} y={950} w={600} size={22} color={INK2}>
            l’opérateur « poisson » : la couture est faite par flatMap
          </Tag>
        </div>
      )}
    </SceneShell>
  );
}

// ── 09 · ReaderT ────────────────────────────────────────────────────────────
const CODE_READERT = [
  'final case class ReaderT[F[_], R, A](run: R => F[A]):',
  '  def flatMap[B](f: A => ReaderT[F, R, B])',
  '                (using Monad[F]): ReaderT[F, R, B] =',
  { s: '    ReaderT(r => run(r).flatMap(a => f(a).run(r)))', hl: BLUE },
  '',
  'case class Config(dbUrl: String, apiKey: String)',
  '',
  'def users:  ReaderT[IO, Config, List[User]]',
  'def render: List[User] => ReaderT[IO, Config, Html]',
  '',
  'val page = users.flatMap(render)',
  { s: 'page.run(Config("jdbc:pg://…", "s3cret"))', hl: GREEN },
];

function SceneReaderT() {
  return (
    <SceneShell ch={CH.readert} code={CODE_READERT} codeTitle="ReaderT.scala" codeFontSize={20.5}
      takeaway="On compose d’abord — l’environnement n’est fourni qu’une fois, à la fin.">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          {/* l'environnement */}
          <Box x={790} y={230} w={340} h={130} label="Config" color={AMBER} t={t} delay={3.0} fill="#f7efdd" />
          <Cap t={t} from={3.6} x={820} y={272} w={280} size={22} font={MONO} color={INK2}>dbUrl · apiKey</Cap>

          {/* le pipeline qui en dépend */}
          <Box x={250} y={520} w={330} h={160} label="users" color={GREEN} t={t} delay={4.2} labelSize={20} />
          <Box x={790} y={520} w={330} h={160} label="enrich" color={GREEN} t={t} delay={4.5} labelSize={20} />
          <Box x={1330} y={520} w={330} h={160} label="render" color={GREEN} t={t} delay={4.8} labelSize={20} />
          <Arrow x1={590} y1={600} x2={775} y2={600} color={INK} width={3} t={t} delay={5.4} dur={0.5} />
          <Arrow x1={1130} y1={600} x2={1315} y2={600} color={INK} width={3} t={t} delay={5.8} dur={0.5} />

          {/* chaque étape réclame la config */}
          <Arrow x1={870} y1={370} x2={440} y2={505} bend={40} color={AMBER} width={3} dashed={true} t={t} delay={6.6} dur={0.8} />
          <Arrow x1={960} y1={370} x2={950} y2={505} color={AMBER} width={3} dashed={true} t={t} delay={6.9} dur={0.8} />
          <Arrow x1={1050} y1={370} x2={1480} y2={505} bend={-40} color={AMBER} width={3} dashed={true} t={t} delay={7.2} dur={0.8} />
          <Cap t={t} from={8.2} to={12.4} x={460} y={760} w={1000} size={31} color={INK}>
            chaque étape a besoin de l’environnement —
            le passer <b>à la main, partout</b> ?
          </Cap>

          {/* ReaderT : R => F[A] */}
          <Cap t={t} from={12.8} to={16.0} x={460} y={880} w={1000} size={30} italic={true} color={INK2}>
            <span style={{ fontFamily: MONO, color: GREEN, fontWeight: 600 }}>ReaderT</span> :
            chaque étape devient <span style={{ fontFamily: MONO }}>R =&gt; F[A]</span>
          </Cap>

          {/* injection unique à la fin */}
          <Box x={210} y={930} w={1180} h={140} label="ReaderT[F, Config, Html]" color={GREEN} t={t} delay={16.2} dashed={true} labelSize={20} />
          <Cap t={t} from={16.8} x={340} y={972} w={920} size={25} font={MONO} weight={500} color={INK2}>
            users  ─  enrich  ─  render      // composé, sans config
          </Cap>
          <Arrow x1={1400} y1={1000} x2={1560} y2={1000} color={AMBER} width={4} t={t} delay={17.8}
            label=".run(config)" labelDy={-24} labelSize={22} />
          <Token x={1640} y={1000} r={42} label="Html" color={GREEN} t={t} delay={18.7} fontSize={20} />
          <Tag t={t} from={19.2} to={tCode + 1} x={1280} y={1080} w={560} size={22} color={INK2}>
            l’environnement, injecté une seule fois
          </Tag>
        </div>
      )}
    </SceneShell>
  );
}

// ── 10 · FunctionK / Natural Transformation ─────────────────────────────────
const CODE_FUNK = [
  'trait ~>[F[_], G[_]]:',
  { s: '  def apply[A](fa: F[A]): G[A]', hl: BLUE },
  '',
  'val optionToList: Option ~> List = new:',
  '  def apply[A](fa: Option[A]): List[A] = fa.toList',
  '',
  '// Scala 3 : fonction polymorphique',
  { s: 'val opt2list: [A] => Option[A] => List[A] =', hl: GREEN },
  '  [A] => (fa: Option[A]) => fa.toList',
  '',
  'optionToList(Some(42))   // List(42)',
  '// la même transformation, pour TOUT type A',
];

function SceneFunK() {
  return (
    <SceneShell ch={CH.funk} code={CODE_FUNK} codeTitle="FunctionK.scala" codeFontSize={21}
      takeaway="F ~> G change le contexte, jamais les valeurs — pour tous les A à la fois.">
      {({ t, tCode }) => (
        <div style={{ position: 'absolute', inset: 0 }}>
          {/* la transformation */}
          <Box x={280} y={300} w={360} h={250} label="Option[A]" color={BLUE} t={t} delay={3.0} />
          <Token x={460} y={430} r={42} label="42" color={INK} t={t} delay={3.6} fontSize={26} />
          <Arrow x1={660} y1={425} x2={1010} y2={425} bend={-40} color={AMBER} width={5} t={t} delay={4.8} dur={0.9}
            label="F ~> G" labelDy={-30} labelSize={28} />
          <Box x={1040} y={300} w={420} h={250} label="List[A]" color={GREEN} t={t} delay={5.8} />
          <Cap t={t} from={6.4} x={1100} y={400} w={120} size={50} font={MONO} color={INK}>[</Cap>
          <Token x={1250} y={430} r={42} label="42" color={INK} t={t} delay={6.6} fontSize={26} />
          <Cap t={t} from={6.9} x={1310} y={400} w={120} size={50} font={MONO} color={INK}>]</Cap>
          <Cap t={t} from={7.8} to={11.6} x={460} y={650} w={1000} size={31} color={INK}>
            on change le <b>conteneur</b> — la valeur, elle, ne bouge pas
          </Cap>

          {/* universalité */}
          <Cap t={t} from={12.0} to={15.8} x={460} y={650} w={1000} size={30} italic={true} color={INK2}>
            et ce, pour <b>tous</b> les types A, d’un seul geste :
          </Cap>
          {[['Int', 13.0], ['String', 13.5], ['User', 14.0]].map(([ty, d], i) => (
            <div key={ty}>
              <Tag t={t} from={d} x={260} y={770 + i * 78} w={360} align="left" size={25} color={BLUE}>Option[{ty}]</Tag>
              <Arrow x1={620} y1={783 + i * 78} x2={830} y2={783 + i * 78} color={AMBER} width={3} t={t} delay={d + 0.2} dur={0.5} />
              <Tag t={t} from={d + 0.6} x={860} y={770 + i * 78} w={360} align="left" size={25} color={GREEN}>List[{ty}]</Tag>
            </div>
          ))}
          <Cap t={t} from={14.8} to={tCode} x={1180} y={830} w={420} size={28} italic={true} color={AMBER} weight={600}>
            une seule définition,<br></br>quantifiée sur A
          </Cap>

          {/* le carré de naturalité */}
          <Tag t={t} from={tCode + 6.5} x={120} y={1180} w={520} align="left" size={24} color={INK2}>
            naturalité —
          </Tag>
          <Tag t={t} from={tCode + 7.2} x={120} y={1230} w={760} align="left" size={24} color={INK2}>
            nt(fa.map(f)) == nt(fa).map(f)
          </Tag>
          <Tag t={t} from={tCode + 8.2} x={120} y={1285} w={760} align="left" size={22} color={INK3}>
            (transformer puis mapper = mapper puis transformer)
          </Tag>
        </div>
      )}
    </SceneShell>
  );
}

Object.assign(window, { SceneKleisli, SceneReaderT, SceneFunK });
