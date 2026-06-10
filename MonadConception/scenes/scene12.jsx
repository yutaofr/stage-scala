// scene12.jsx — 第 1 幕：开场名言（0–38s）；第 2 幕：范畴（38–104s）

// 名言展示：四个术语按 hlAt 时刻染色 + 下划线
function QuoteLine({ y = 430, size = 62, from = 0, hlAt = null }) {
  const t = useT();
  const parts = [
    { txt: '一个' }, { txt: '单子', c: MV.mnd, k: 0 },
    { txt: '，说白了就是' },
    { txt: '自函子', c: MV.fun, k: 1 },
    { txt: '范畴', c: MV.cat, k: 2 },
    { txt: '上的一个' },
    { txt: '幺半群', c: MV.mon, k: 3 },
    { txt: '。' },
  ];
  return (
    <div style={{
      position: 'absolute', left: 0, right: 0, top: y, textAlign: 'center',
      fontFamily: MV.serif, fontSize: size, fontWeight: 700, color: MV.ink,
      letterSpacing: '0.04em', lineHeight: 1.6,
    }}>
      <span style={{ color: MV.faint, fontWeight: 400, marginRight: 6 }}>「</span>
      {parts.map((p, i) => {
        if (p.c == null) return <span key={i}>{p.txt}</span>;
        const hp = hlAt == null ? 1 : ev(t, hlAt[p.k], 0.6, Easing.easeOutCubic);
        const col = hp > 0.4 ? p.c : MV.ink;
        return (
          <span key={i} style={{ position: 'relative', color: col, transition: 'color 300ms', display: 'inline-block' }}>
            {p.txt}
            <span style={{
              position: 'absolute', left: '4%', bottom: -6, height: 5, borderRadius: 3,
              width: `${hp * 92}%`, background: p.c, opacity: hp,
            }}></span>
          </span>
        );
      })}
      <span style={{ color: MV.faint, fontWeight: 400, marginLeft: 6 }}>」</span>
    </div>
  );
}

// ── 第 1 幕：开场（0–38s）────────────────────────────────────
function Scene1() {
  const t = useT();
  const roadNodes = [
    { zh: '范畴', c: MV.cat, at: 22.0 },
    { zh: '函子', c: MV.fun, at: 23.6 },
    { zh: '自函子', c: MV.fun, at: 25.2 },
    { zh: '幺半群', c: MV.mon, at: 26.8 },
    { zh: '单子', c: MV.mnd, at: 28.4 },
  ];
  const rx = (i) => 400 + i * 280;
  return (
    <Sprite start={0} end={38}>
      <FadeGroup from={0.4} to={32.5} exit={1.2}>
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 300, textAlign: 'center',
          fontFamily: MV.mono, fontSize: 22, letterSpacing: '0.4em', color: MV.faint,
          opacity: ev(t, 1.0, 1.0),
        }}>A MONAD IS A MONOID IN THE CATEGORY OF ENDOFUNCTORS</div>
        <QuoteLine y={390} hlAt={[10.2, 7.0, 7.8, 8.8]} />
      </FadeGroup>

      {/* 三个陌生词的小卡片 */}
      <FadeGroup from={14} to={32.5} exit={1.0}>
        {[
          { zh: '范畴', en: 'Category', c: MV.cat, x: 560 },
          { zh: '（自）函子', en: 'Endofunctor', c: MV.fun, x: 960 },
          { zh: '幺半群', en: 'Monoid', c: MV.mon, x: 1360 },
        ].map((d, i) => {
          const p = ev(t, 14.4 + i * 0.5, 0.6, Easing.easeOutBack);
          return (
            <div key={i} style={{
              position: 'absolute', left: d.x, top: 620, transform: `translate(-50%,-50%) scale(${0.7 + 0.3 * p})`,
              opacity: Math.min(1, p * 1.4),
              width: 300, padding: '22px 0', textAlign: 'center',
              border: `2px solid ${d.c}55`, borderRadius: 16, background: `${d.c}0d`,
            }}>
              <div style={{ fontFamily: MV.serif, fontSize: 38, fontWeight: 700, color: d.c }}>{d.zh}</div>
              <div style={{ fontFamily: MV.mono, fontSize: 20, color: MV.dim, marginTop: 8 }}>{d.en}</div>
            </div>
          );
        })}
      </FadeGroup>

      {/* 路线图 */}
      <FadeGroup from={21.5} to={37.2} exit={0.9}>
        <SvgLayer>
          {roadNodes.slice(0, -1).map((n, i) => (
            <Arrow key={i} x1={rx(i)} y1={812} x2={rx(i + 1)} y2={812}
              color={MV.dim} width={3.5} from={n.at + 0.7} dur={0.7} shrink={62} />
          ))}
        </SvgLayer>
        {roadNodes.map((n, i) => {
          const p = ev(t, n.at, 0.55, Easing.easeOutBack);
          const lit = i === 4 ? ev(t, 29.4, 0.8) : 1;
          return (
            <div key={i} style={{
              position: 'absolute', left: rx(i), top: 812, transform: `translate(-50%,-50%) scale(${0.6 + 0.4 * p})`,
              opacity: Math.min(1, p * 1.3),
              width: i === 4 ? 132 : 112, height: i === 4 ? 132 : 112, borderRadius: '50%',
              border: `2.5px solid ${n.c}`, background: `${n.c}${i === 4 ? '2e' : '14'}`,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontFamily: MV.serif, fontSize: i === 4 ? 34 : 27, fontWeight: 700, color: n.c,
              boxShadow: i === 4 ? `0 0 ${30 + 30 * lit}px ${n.c}55` : `0 0 18px ${n.c}22`,
            }}>{n.zh}</div>
          );
        })}
      </FadeGroup>

      <Cap from={1.2} to={6.6}>在函数式编程的世界里，流传着一句臭名昭著的话。</Cap>
      <Cap from={6.6} to={13.4}>据说它能在三秒之内，劝退最自信的工程师。<Em c={MV.mnd}>单子</Em>？<Em c={MV.fun}>自函子</Em>？<Em c={MV.mon}>幺半群</Em>？</Cap>
      <Cap from={13.4} to={21.2}>但仔细看——这句话里，真正陌生的概念其实只有<Em>三个</Em>。</Cap>
      <Cap from={21.2} to={31.8}>我们沿着这条路，一个一个拆开。每一个，你其实都在 Scala 里天天打交道。</Cap>
      <Cap from={31.8} to={37.4}>先从最底下的地基说起：什么是<Em c={MV.cat}>范畴</Em>？</Cap>
    </Sprite>
  );
}

// ── 第 2 幕：范畴（38–104s）──────────────────────────────────
function Scene2() {
  const t = useT();
  const S = { x: 540, y: 580 }, I = { x: 960, y: 395 }, B = { x: 1380, y: 580 };
  // 94s 后的“星座”扩展
  const extra = [
    { x: 280, y: 330, l: 'List[Int]' }, { x: 680, y: 210, l: 'Double' },
    { x: 1280, y: 200, l: 'User' }, { x: 1650, y: 330, l: 'Unit' },
    { x: 240, y: 720, l: 'Char' }, { x: 1690, y: 730, l: 'BigDecimal' },
  ];
  const constFrom = 94.2;
  return (
    <Sprite start={38} end={104}>
      <Chapter from={38.5} to={103} num="Chapter 01" zh="范畴" en="Category" color={MV.cat} />

      <FadeGroup from={39} to={103.2} exit={0.8}>
        <SvgLayer>
          {/* f: String => Int */}
          <Arrow x1={S.x} y1={S.y} x2={I.x} y2={I.y} bend={30} color={MV.ink} from={47} dur={0.9}
            label="_.length" shrink={46} labelDy={-20} />
          {/* g: Int => Boolean */}
          <Arrow x1={I.x} y1={I.y} x2={B.x} y2={B.y} bend={30} color={MV.ink} from={55} dur={0.9}
            label="_ % 2 == 0" shrink={46} labelDy={-20} />
          {/* 复合 */}
          <Arrow x1={S.x} y1={S.y} x2={B.x} y2={B.y} bend={-130} color={MV.cat} width={4.5} from={64} dur={1.1}
            label="length andThen isEven" shrink={46} labelDy={42} />
          {/* identity 自环 */}
          <IdLoop x={S.x} y={S.y - 36} from={73.5} color={MV.dim} />
          <IdLoop x={I.x} y={I.y - 36} from={74.1} color={MV.dim} />
          <IdLoop x={B.x} y={B.y - 36} from={74.7} color={MV.dim} />
          {/* 星座的暗箭头 */}
          {[[extra[0], extra[1]], [extra[1], { x: I.x, y: I.y - 40, l: '' }], [extra[2], extra[3]],
            [{ x: S.x, y: S.y, l: '' }, extra[4]], [extra[2], { x: B.x, y: B.y, l: '' }], [extra[5], extra[3]]].map((pr, i) => (
            <Arrow key={i} x1={pr[0].x} y1={pr[0].y} x2={pr[1].x} y2={pr[1].y} bend={24}
              color={MV.faint} width={2.5} from={constFrom + 0.5 + i * 0.16} dur={0.7} shrink={40} opacity={0.65} />
          ))}
        </SvgLayer>

        <NodeDot x={S.x} y={S.y} label="String" color={MV.cat} from={40.5} />
        <NodeDot x={I.x} y={I.y} label="Int" color={MV.cat} from={41.2} />
        <NodeDot x={B.x} y={B.y} label="Boolean" color={MV.cat} from={41.9} />
        {extra.map((e, i) => (
          <NodeDot key={i} x={e.x} y={e.y} label={e.l} color={MV.cat} from={constFrom + i * 0.14}
            size={12} fontSize={21} dimLabel opacity={0.75} />
        ))}
      </FadeGroup>

      {/* 两条规则 */}
      <FadeGroup from={83} to={94.5} exit={0.6}>
        <div style={{
          position: 'absolute', left: '50%', top: 760, transform: 'translateX(-50%)',
          display: 'flex', flexDirection: 'column', gap: 16, alignItems: 'center',
          fontFamily: MV.mono, fontSize: 25, color: MV.dim, whiteSpace: 'nowrap',
        }}>
          <div style={{ opacity: ev(t, 83.4, 0.6) }}>
            <span style={{ color: MV.cat }}>(f andThen g) andThen h</span>
            <span style={{ color: MV.faint }}>  ==  </span>
            <span style={{ color: MV.cat }}>f andThen (g andThen h)</span>
          </div>
          <div style={{ opacity: ev(t, 86.5, 0.6) }}>
            <span style={{ color: MV.dim }}>identity andThen f  ==  f  ==  f andThen identity</span>
          </div>
        </div>
      </FadeGroup>

      {/* 总结横幅 */}
      <FadeGroup from={96} to={103.2} exit={0.8}>
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 868, textAlign: 'center',
          fontFamily: MV.serif, fontSize: 40, fontWeight: 700, color: MV.ink, letterSpacing: '0.05em',
        }}>
          对象 <span style={{ color: MV.faint }}>+</span> 箭头 <span style={{ color: MV.faint }}>+</span> 复合 <span style={{ color: MV.faint }}>+</span> 恒等
          <span style={{ color: MV.faint, margin: '0 16px' }}>⇒</span>
          <span style={{ color: MV.cat }}>一个范畴</span>
        </div>
      </FadeGroup>

      <Cap from={39.5} to={46.8}>把每一个 Scala 类型看成一个点。范畴论管这些点叫——<Em c={MV.cat}>对象</Em>。</Cap>
      <Cap from={46.8} to={54.8}>类型之间的函数，就是点与点之间的<Em c={MV.cat}>箭头</Em>。比如 <Em>String =&gt; Int</Em>。</Cap>
      <Cap from={54.8} to={63.6}>再来一条 <Em>Int =&gt; Boolean</Em>。箭头开始多起来了。</Cap>
      <Cap from={63.6} to={73.2}>首尾相接的箭头可以<Em c={MV.cat}>复合</Em>出一条新箭头——这就是你写过无数次的 <Em>andThen</Em>。</Cap>
      <Cap from={73.2} to={82.8}>每个对象还自带一条原地打转的箭头：<Em>identity</Em>。它什么都不做，却不可或缺。</Cap>
      <Cap from={82.8} to={93.8}>规则只有两条：复合满足<Em c={MV.cat}>结合律</Em>，identity 是复合的<Em c={MV.cat}>单位</Em>。仅此而已。</Cap>
      <Cap from={93.8} to={103.4}>对象、箭头、复合、恒等——这就是范畴的全部。你写的每一行 Scala，都活在<Em c={MV.cat}>类型与函数构成的范畴</Em>里。</Cap>
    </Sprite>
  );
}

Object.assign(window, { Scene1, Scene2, QuoteLine });
