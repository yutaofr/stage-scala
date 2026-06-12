// scene34.jsx — 第 3 幕：函子（104–170s）；第 4 幕：自函子（170–218s）

// 世界面板（横向板块）
function WorldPanel({ x, y, w, h, color, label, from, opacity = 1 }) {
  const t = useT();
  const p = ev(t, from, 0.8, Easing.easeOutCubic);
  if (p <= 0) return null;
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: w, height: h,
      border: `1.5px solid ${color}44`, borderRadius: 24,
      background: `linear-gradient(180deg, ${color}0a, ${color}04)`,
      opacity: opacity * p, transform: `translateY(${(1 - p) * 18}px)`,
    }}>
      <div style={{
        position: 'absolute', top: 22, left: 34, fontFamily: MV.sans, fontSize: 25,
        fontWeight: 500, color, letterSpacing: '0.12em', whiteSpace: 'nowrap',
      }}>{label}</div>
    </div>
  );
}

// ── 第 3 幕：函子（104–170s）─────────────────────────────────
function Scene3() {
  const t = useT();
  const X = { s: 560, i: 1080, b: 1560 };
  const YL = 745, YU = 330; // 下层 / 上层节点高度
  const showB = 134; // Boolean 列出现
  return (
    <Sprite start={104} end={170}>
      <Chapter from={104.5} to={169} num="Chapter 02" zh="函子" fr="Foncteur" en="Functor" color={MV.fun} />

      <FadeGroup from={105} to={169.2} exit={0.8}>
        {/* 下层：普通世界 */}
        <WorldPanel x={280} y={620} w={1360} h={260} color={MV.cat} label={tr('普通的类型世界', 'Monde des types ordinaires')} from={105.5} />
        {/* 上层：Option 世界 */}
        <WorldPanel x={280} y={205} w={1360} h={260} color={MV.fun} label={tr('Option 的世界', "Monde d'Option")} from={113} />

        <SvgLayer>
          {/* 下层箭头 f */}
          <Arrow x1={X.s} y1={YL} x2={X.i} y2={YL} bend={-44} color={MV.cat} from={108} dur={0.8}
            label="f" labelDy={-18} shrink={50} />
          {/* 垂直搬运（对象） */}
          <Arrow x1={X.s} y1={YL - 56} x2={X.s} y2={YU + 40} color={MV.fun} width={3.5} from={114.5} dur={0.9}
            dashed shrink={26} />
          <Arrow x1={X.i} y1={YL - 56} x2={X.i} y2={YU + 40} color={MV.fun} width={3.5} from={115.4} dur={0.9}
            dashed shrink={26} />
          {/* 上层箭头 map(f) */}
          <Arrow x1={X.s} y1={YU} x2={X.i} y2={YU} bend={-44} color={MV.fun} width={4.5} from={123.5} dur={0.9}
            label="map(f)" labelDy={-20} shrink={70} />
          {/* g 列（结构保持段） */}
          <Arrow x1={X.i} y1={YL} x2={X.b} y2={YL} bend={-44} color={MV.cat} from={showB + 1.6} dur={0.8}
            label="g" labelDy={-18} shrink={50} />
          <Arrow x1={X.i} y1={YU} x2={X.b} y2={YU} bend={-44} color={MV.fun} width={4.5} from={showB + 2.4} dur={0.8}
            label="map(g)" labelDy={-20} shrink={70} />
          {/* 复合对照：下层 g∘f 弧 + 上层 map(f andThen g) 弧 */}
          <Arrow x1={X.s} y1={YL} x2={X.b} y2={YL} bend={86} color={MV.cat} width={3} from={showB + 4.2} dur={1.0}
            label="f andThen g" labelDy={34} shrink={50} opacity={0.85} />
          <Arrow x1={X.s} y1={YU} x2={X.b} y2={YU} bend={86} color={MV.mon} width={4.5} from={showB + 5.4} dur={1.0}
            label="map(f andThen g)" labelDy={36} shrink={70} />
          {/* 恒等保持 */}
          <IdLoop x={X.s} y={YL - 16} r={26} from={151.5} color={MV.cat} label="id" opacity={0.9} />
          <IdLoop x={X.s} y={YU - 16} r={26} from={152.3} color={MV.fun} label="map(id)" opacity={0.9} />
        </SvgLayer>

        {/* 下层节点 */}
        <NodeDot x={X.s} y={YL} label="String" color={MV.cat} from={106.5} />
        <NodeDot x={X.i} y={YL} label="Int" color={MV.cat} from={107.2} />
        <NodeDot x={X.b} y={YL} label="Boolean" color={MV.cat} from={showB + 1.0} />
        {/* 上层节点 */}
        <NodeDot x={X.s} y={YU} label="Option[String]" color={MV.fun} from={116.0} />
        <NodeDot x={X.i} y={YU} label="Option[Int]" color={MV.fun} from={116.8} />
        <NodeDot x={X.b} y={YU} label="Option[Boolean]" color={MV.fun} from={showB + 3.2} />

        {/* 中缝的搬运机标签 */}
        <FadeGroup from={117.5} to={169} enter={0.6}>
          <div style={{
            position: 'absolute', left: 180, top: 520, transform: 'translateY(-50%)',
            fontFamily: MV.mono, fontSize: 30, fontWeight: 700, color: MV.fun,
            display: 'flex', alignItems: 'center', gap: 14, whiteSpace: 'nowrap',
          }}>
            <span style={{ fontSize: 40 }}>F</span>
            <span style={{ color: MV.dim, fontSize: 22, fontFamily: MV.sans }}>↑ {tr('搬运', 'transport')}</span>
          </div>
        </FadeGroup>
      </FadeGroup>

      {/* 结构保持等式 */}
      <FadeGroup from={141.5} to={151} exit={0.5}>
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 512, textAlign: 'center',
          fontFamily: MV.mono, fontSize: 30, fontWeight: 600,
        }}>
          <span style={{ color: MV.mon }}>map(f andThen g)</span>
          <span style={{ color: MV.faint }}>  ==  </span>
          <span style={{ color: MV.fun }}>map(f) andThen map(g)</span>
        </div>
      </FadeGroup>
      <FadeGroup from={153.5} to={161.5} exit={0.5}>
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 512, textAlign: 'center',
          fontFamily: MV.mono, fontSize: 30, fontWeight: 600,
        }}>
          <span style={{ color: MV.fun }}>map(identity)</span>
          <span style={{ color: MV.faint }}>  ==  </span>
          <span style={{ color: MV.fun }}>identity</span>
        </div>
      </FadeGroup>

      {/* 小结 */}
      <FadeGroup from={162.5} to={169.2} exit={0.7}>
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 505, textAlign: 'center',
          fontFamily: MV.serif, fontSize: 38, fontWeight: 700, color: MV.ink, letterSpacing: '0.04em',
        }}>
          {tr('函子', 'Foncteur')} <span style={{ color: MV.faint }}>=</span> <span style={{ color: MV.fun }}>{tr('搬对象', 'transporter les objets')}</span>
          <span style={{ color: MV.faint }}> + </span><span style={{ color: MV.fun }}>{tr('搬箭头', 'transporter les flèches')}</span>
          <span style={{ color: MV.faint }}> + </span><span style={{ color: MV.mon }}>{tr('不搬坏结构', 'préserver la structure')}</span>
        </div>
      </FadeGroup>

      <Cap from={105.5} to={113.2} fr={<>Nous avons terminé l'histoire à l'intérieur d'une catégorie. Question suivante : peut-on tracer une « flèche » <Em c={MV.fun}>entre</Em> deux catégories ?</>}>范畴内部的故事讲完了。下一个问题：范畴与范畴<Em c={MV.fun}>之间</Em>，能不能也连一条“箭头”？</Cap>
      <Cap from={113.2} to={122.8} fr={<><Em c={MV.fun}>Un foncteur</Em> est cette machine de transport de mondes : il transforme chaque type A en Option[A].</>}><Em c={MV.fun}>函子</Em>就是这样一台“世界搬运机”：它把每个类型 A，搬成 Option[A]。</Cap>
      <Cap from={122.8} to={133.6} fr={<>Plus important : il transporte aussi les flèches. f devient <Em c={MV.fun}>map(f)</Em>. Oui, c'est votre <Em>.map</Em> familier.</>}>更重要的是，它连箭头也一起搬：f 被抬升成 <Em c={MV.fun}>map(f)</Em>。没错——这就是你熟悉的 <Em>.map</Em>。</Cap>
      <Cap from={133.6} to={144.8} fr={<>Mais le transport a une règle stricte : <Em c={MV.mon}>ne pas casser la structure</Em>. Composer puis transporter doit égaler transporter puis composer.</>}>但搬运有军规：<Em c={MV.mon}>不能搬坏结构</Em>。先复合再搬，必须等于先搬再复合。</Cap>
      <Cap from={144.8} to={152.6} fr="Les deux chemins arrivent au même résultat : c'est la première loi du foncteur.">两条路殊途同归——这正是函子定律的第一条。</Cap>
      <Cap from={152.6} to={161.5} fr="Deuxième loi : l'identité transportée doit rester une identité. Ce qui ne fait rien doit encore ne rien faire.">第二条：恒等箭头搬过去，还得是恒等。什么都不做的，搬完也得什么都不做。</Cap>
      <Cap from={161.5} to={169.4} fr={<>En une phrase : un foncteur est une application entre catégories qui <Em c={MV.fun}>préserve la structure</Em>.</>}>一句话总结：函子是范畴之间<Em c={MV.fun}>保持结构的映射</Em>。</Cap>
    </Sprite>
  );
}

// ── 第 4 幕：自函子（170–218s）───────────────────────────────
function Scene4() {
  const t = useT();
  const C = { x: 600, y: 545, r: 250 }; // 大范畴圆
  const inner = [
    [-110, -80, 'Int'], [60, -130, 'String'], [120, 40, 'User'],
    [-60, 90, 'List[A]'], [-150, 10, ''], [30, -20, ''],
  ];
  const badges = [
    { l: 'Option[_]', x: 1320, y: 300 }, { l: 'List[_]', x: 1580, y: 300 },
    { l: 'Future[_]', x: 1320, y: 392 }, { l: 'Either[E, _]', x: 1590, y: 392 },
    { l: 'IO[_]', x: 1320, y: 484 },
  ];
  const nestFrom = 193; // 嵌套演示
  const loopP = ev(t, 173, 1.4);
  return (
    <Sprite start={170} end={218}>
      <Chapter from={170.5} to={217} num="Chapter 03" zh="自函子" fr="Endofoncteur" en="Endofunctor" color={MV.fun} />

      <FadeGroup from={171} to={217.2} exit={0.8}>
        {/* 大范畴圆 */}
        <div style={{
          position: 'absolute', left: C.x - C.r, top: C.y - C.r, width: C.r * 2, height: C.r * 2,
          borderRadius: '50%', border: `2.5px solid ${MV.cat}55`,
          background: `radial-gradient(circle at 40% 35%, ${MV.cat}10, transparent 70%)`,
          opacity: ev(t, 171.3, 0.8), boxShadow: `0 0 60px ${MV.cat}14`,
        }}></div>
        <div style={{
          position: 'absolute', left: C.x, top: C.y + C.r + 36, transform: 'translateX(-50%)',
          fontFamily: MV.sans, fontSize: 26, color: MV.cat, letterSpacing: '0.1em',
          opacity: ev(t, 171.8, 0.8), whiteSpace: 'nowrap',
        }}>{tr('Scala 的类型范畴', 'Catégorie des types Scala')}</div>
        {inner.map((n, i) => (
          <NodeDot key={i} x={C.x + n[0]} y={C.y + n[1]} label={n[2]} color={MV.cat}
            from={172 + i * 0.12} size={11} fontSize={19} dimLabel opacity={0.7} />
        ))}

        {/* 自环大箭头：从圆顶出发，绕一圈回到圆顶 */}
        <SvgLayer>
          <Arrow x1={C.x - 100} y1={C.y - C.r + 18} x2={C.x + 100} y2={C.y - C.r + 18}
            bend={170} color={MV.fun} width={5} from={173} dur={1.5} shrink={8} />
        </SvgLayer>
        <div style={{
          position: 'absolute', left: C.x, top: C.y - C.r - 165, transform: 'translateX(-50%)',
          fontFamily: MV.mono, fontSize: 40, fontWeight: 700, color: MV.fun,
          opacity: loopP > 0.6 ? (loopP - 0.6) / 0.4 : 0,
          textShadow: `0 0 30px ${MV.fun}66`,
        }}>F[_]</div>
        <div style={{
          position: 'absolute', left: C.x, top: C.y - C.r - 110, transform: 'translateX(-50%)',
          fontFamily: MV.sans, fontSize: 23, color: MV.dim,
          opacity: ev(t, 175, 0.8), whiteSpace: 'nowrap',
        }}>{tr('出发点 = 目的地 ⇒ 自函子', 'départ = arrivée ⇒ endofoncteur')}</div>

        {/* 熟面孔徽章 */}
        {badges.map((b, i) => (
          <Badge key={i} x={b.x} y={b.y} label={b.l} color={MV.fun} from={181 + i * 0.55} />
        ))}

        {/* 嵌套：Option[Option[Int]] */}
        <BoxC x={1180} y={580} w={560} h={290} color={MV.fun} label="Option" from={nestFrom}>
          <BoxC x={130} y={62} w={300} h={170} color={MV.fun} label="Option" from={nestFrom + 1.2} labelSize={20} style={{ position: 'absolute' }}>
            <ValDot x={150} y={88} label="42" from={nestFrom + 2.2} style={{ position: 'absolute' }} />
          </BoxC>
        </BoxC>
        <FadeGroup from={nestFrom + 3.0} to={217.2}>
          <div style={{
            position: 'absolute', left: 1460, top: 922, transform: 'translateX(-50%)',
            fontFamily: MV.mono, fontSize: 27, color: MV.ink, whiteSpace: 'nowrap',
          }}>
            <span style={{ color: MV.fun }}>Option[Option[</span>Int<span style={{ color: MV.fun }}>]]</span>
            <span style={{ color: MV.faint }}>　·　</span>
            <span style={{ color: MV.dim }}>Future[Future[T]]</span>
          </div>
        </FadeGroup>

        {/* F[F[A]] ⇒ ? */}
        <FadeGroup from={207} to={217.2}>
          <div style={{
            position: 'absolute', left: 600, top: 880, transform: 'translateX(-50%)',
            fontFamily: MV.mono, fontSize: 46, fontWeight: 700, whiteSpace: 'nowrap',
            opacity: 0.92 + 0.08 * Math.sin(t * 4),
          }}>
            <span style={{ color: MV.fun }}>F[F[A]]</span>
            <span style={{ color: MV.faint }}> ⇒ </span>
            <span style={{ color: MV.mnd }}>?</span>
          </div>
        </FadeGroup>
      </FadeGroup>

      <Cap from={171.5} to={180.4} fr={<>Si une machine de transport part et arrive dans <Em c={MV.cat}>la même catégorie</Em>, elle s'appelle un <Em c={MV.fun}>endofoncteur</Em>.</>}>如果一台搬运机的出发点和目的地是<Em c={MV.cat}>同一个范畴</Em>——它就叫<Em c={MV.fun}>自函子</Em>。</Cap>
      <Cap from={180.4} to={192.4} fr={<>En Scala, vous en connaissez beaucoup : chaque <Em c={MV.fun}>F[_]</Em> transporte le monde des types vers lui-même.</>}>在 Scala 里你认识一大把：每一个 <Em c={MV.fun}>F[_]</Em>，都是从类型世界搬回类型世界的自函子。</Cap>
      <Cap from={192.4} to={205.8} fr={<>Un endofoncteur a un pouvoir subtil : comme l'arrivée reste au même endroit, il peut <Em c={MV.mon}>s'appliquer encore une fois</Em>. L'imbrication apparaît.</>}>自函子有个微妙的本事：终点还在原地，所以它可以<Em c={MV.mon}>再作用一次</Em>——于是出现了嵌套。</Cap>
      <Cap from={205.8} to={217.4} fr={<><Em>Option[Option[Int]]</Em>, <Em>Future[Future[T]]</Em>... gardez en tête ce <Em c={MV.mnd}>malaise de l'imbrication</Em>. Toute l'histoire de la monade sert à le dissoudre.</>}>Option[Option[Int]]、Future[Future[T]]……记住这种嵌套的<Em c={MV.mnd}>不适感</Em>。单子的整个故事，就是为了化解它。</Cap>
    </Sprite>
  );
}

Object.assign(window, { Scene3, Scene4, WorldPanel });
