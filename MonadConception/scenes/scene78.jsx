// scene78.jsx — 第 7 幕：回到 Scala（388–452s）；第 8 幕：终幕（452–484s）

// ── 第 7 幕：你每天都在写它 ───────────────────────────────────
function Scene7() {
  const t = useT();
  const c2 = 398.5, c3 = 409, c4 = 424, c5 = 440;
  const traitLines = [
    [['trait ', 'kw'], ['Monad', 'ty'], ['[', 'pl'], ['F[_]', 'ty'], ['] {', 'pl']],
    [['  def ', 'kw'], ['pure', 'fn'], ['[A](a: A): ', 'pl'], ['F[A]', 'ty']],
    [['  def ', 'kw'], ['flatMap', 'fn'], ['[A, B](fa: ', 'pl'], ['F[A]', 'ty'], [')(f: A => ', 'pl'], ['F[B]', 'ty'], ['): ', 'pl'], ['F[B]', 'ty']],
    [['}', 'pl']],
  ];
  const forLines = [
    [['for', 'kw'], [' {', 'pl']],
    [['  user ', 'pl'], ['<-', 'op'], [' findUser(id)', 'pl']],
    [['  addr ', 'pl'], ['<-', 'op'], [' user.address', 'pl']],
    [['  city ', 'pl'], ['<-', 'op'], [' addr.city', 'pl']],
    [['} ', 'pl'], ['yield', 'kw'], [' city', 'pl']],
  ];
  const desugarLines = [
    [['findUser(id).', 'pl'], ['flatMap', 'op'], [' { user =>', 'pl']],
    [['  user.address.', 'pl'], ['flatMap', 'op'], [' { addr =>', 'pl']],
    [['    addr.city.', 'pl'], ['map', 'op'], [' { city => city }', 'pl']],
    [['  }', 'pl']],
    [['}', 'pl']],
  ];
  const instances = [
    { l: 'Option', pure: 'Some(_)', flat: '压平「可能为空」', c: MV.fun },
    { l: 'Either[E, _]', pure: 'Right(_)', flat: '压平「可能失败」', c: MV.cat },
    { l: 'Future', pure: 'successful(_)', flat: '压平「尚未完成」', c: MV.pur },
    { l: 'IO', pure: 'IO.pure(_)', flat: '压平「副作用的计划」', c: MV.mnd },
  ];
  return (
    <Sprite start={388} end={452}>
      <Chapter from={388.5} to={451} num="Chapter 06" zh="回到 Scala" en="" color={MV.mnd} />

      {/* C1+C2：trait + flatMap = map + flatten */}
      <FadeGroup from={389} to={c3 - 0.5} exit={0.6}>
        <CodeBlock x={170} y={310} from={389.5} lines={traitLines} size={29} title="你每天都在用的接口" />
        <div style={{
          position: 'absolute', left: 170, top: 640, fontFamily: MV.mono, fontSize: 30,
          opacity: ev(t, c2, 0.7), whiteSpace: 'nowrap',
        }}>
          <span style={{ color: MV.dim }}>fa.</span><span style={{ color: MV.mnd, fontWeight: 700 }}>flatMap</span><span style={{ color: MV.dim }}>(f)</span>
          <span style={{ color: MV.faint }}>  ==  </span>
          <span style={{ color: MV.dim }}>fa.</span><span style={{ color: MV.fun, fontWeight: 700 }}>map</span><span style={{ color: MV.dim }}>(f).</span><span style={{ color: MV.mon, fontWeight: 700 }}>flatten</span>
        </div>
        {/* 右侧小图：map 产生嵌套，flatten 压平 */}
        <SvgLayer>
          <Arrow x1={1300} y1={385} x2={1300} y2={500} color={MV.fun} width={4} from={c2 + 1.2} dur={0.7} label="map(f)" labelDy={-10} labelSize={23} shrink={14} />
          <Arrow x1={1300} y1={620} x2={1300} y2={735} color={MV.mon} width={4} from={c2 + 3.2} dur={0.7} label="flatten" labelDy={-10} labelSize={23} shrink={14} />
        </SvgLayer>
        {[
          { y: 330, l: 'F[A]', c: MV.fun, at: c2 + 0.6 },
          { y: 560, l: 'F[F[B]]', c: MV.fun, at: c2 + 2.2, warn: true },
          { y: 790, l: 'F[B]', c: MV.mnd, at: c2 + 4.2 },
        ].map((d, i) => (
          <div key={i} style={{
            position: 'absolute', left: 1300, top: d.y, transform: 'translate(-50%,-50%)',
            fontFamily: MV.mono, fontSize: 31, fontWeight: 700, color: d.c,
            border: `2px solid ${d.c}55`, borderRadius: 12, padding: '8px 24px', background: '#131820',
            opacity: ev(t, d.at, 0.5),
          }}>{d.l}{d.warn ? <span style={{ color: MV.dim, fontSize: 23, fontWeight: 400 }}>　← 嵌套又来了</span> : null}</div>
        ))}
      </FadeGroup>

      {/* C3：四大常客 */}
      <FadeGroup from={c3} to={c4 - 0.5} exit={0.6}>
        <div style={{ position: 'absolute', left: 0, right: 0, top: 230, textAlign: 'center', fontFamily: MV.serif, fontSize: 32, fontWeight: 700, color: MV.ink, opacity: ev(t, c3 + 0.3, 0.6) }}>
          同一个幺半群，四副面孔
        </div>
        {instances.map((d, i) => {
          const at = c3 + 1 + i * 1.6;
          const o = ev(t, at, 0.6);
          return (
            <div key={i} style={{
              position: 'absolute', left: 250, top: 320 + i * 130, width: 1420, height: 104,
              display: 'flex', alignItems: 'center', gap: 36,
              border: `1.5px solid ${d.c}44`, borderRadius: 18, background: `${d.c}0a`,
              padding: '0 44px', opacity: o, transform: `translateX(${(1 - o) * 30}px)`,
            }}>
              <div style={{ fontFamily: MV.mono, fontSize: 30, fontWeight: 700, color: d.c, width: 300 }}>{d.l}</div>
              <div style={{ fontFamily: MV.mono, fontSize: 25, color: MV.dim, width: 420 }}>
                pure = <span style={{ color: MV.ink }}>{d.pure}</span>
              </div>
              <div style={{ fontFamily: MV.sans, fontSize: 26, color: MV.ink, whiteSpace: 'nowrap' }}>
                flatten <span style={{ color: MV.dim }}>{d.flat}</span>
              </div>
            </div>
          );
        })}
      </FadeGroup>

      {/* C4：for 推导式 */}
      <FadeGroup from={c4} to={c5 - 0.5} exit={0.6}>
        <CodeBlock x={180} y={350} from={c4 + 0.5} lines={forLines} size={29} title="你写的" />
        <SvgLayer>
          <Arrow x1={830} y1={530} x2={1000} y2={530} color={MV.mon} width={4.5} from={c4 + 4} dur={0.8} label="脱糖" labelDy={-18} labelSize={24} shrink={10} />
        </SvgLayer>
        <CodeBlock x={1030} y={335} from={c4 + 5} lines={desugarLines} size={27} title="编译器看到的" />
      </FadeGroup>

      {/* C5：三定律 */}
      <FadeGroup from={c5} to={451.4} exit={0.7}>
        <div style={{ position: 'absolute', left: 0, right: 0, top: 280, textAlign: 'center', fontFamily: MV.serif, fontSize: 32, fontWeight: 700, color: MV.ink, opacity: ev(t, c5 + 0.3, 0.6) }}>
          三条定律，还是那个幺半群
        </div>
        {[
          { f: 'pure(a).flatMap(f)', e: 'f(a)', n: '左单位', at: c5 + 1.2 },
          { f: 'fa.flatMap(pure)', e: 'fa', n: '右单位', at: c5 + 2.6 },
          { f: 'fa.flatMap(f).flatMap(g)', e: 'fa.flatMap(a => f(a).flatMap(g))', n: '结合律', at: c5 + 4.0 },
        ].map((d, i) => (
          <div key={i} style={{
            position: 'absolute', left: 0, right: 0, top: 400 + i * 120, textAlign: 'center',
            fontFamily: MV.mono, fontSize: 29, opacity: ev(t, d.at, 0.6), whiteSpace: 'nowrap',
          }}>
            <span style={{ color: MV.mon, fontFamily: MV.sans, fontSize: 24, marginRight: 30 }}>{d.n}</span>
            <span style={{ color: MV.ink }}>{d.f}</span>
            <span style={{ color: MV.faint }}>  ==  </span>
            <span style={{ color: MV.ink }}>{d.e}</span>
          </div>
        ))}
      </FadeGroup>

      <Cap from={389.5} to={398.2}>回到 Scala。范畴论给的是 flatten，但工程上我们把 <Em c={MV.fun}>map</Em> 和 <Em c={MV.mon}>flatten</Em> 合成了一步——</Cap>
      <Cap from={398.2} to={408.6}>这就是 <Em c={MV.mnd}>flatMap</Em>。名字早就把真相写在脸上了：map 完会嵌套，所以顺手压平。</Cap>
      <Cap from={408.6} to={423.6}>Option 压平「可能为空」，Either 压平「可能失败」，Future 压平「尚未完成」，IO 压平「副作用的计划」——<Em>同一个数学结构</Em>。</Cap>
      <Cap from={423.6} to={439.6}>for 推导式只是 flatMap 的糖。单子给你的能力，是把一步步<Em c={MV.fun}>装在盒子里的计算</Em>安全地串成流水线。</Cap>
      <Cap from={439.6} to={451.6}>三条定律保证：这条流水线无论怎么加括号、怎么重构，<Em c={MV.mon}>语义都不变</Em>。</Cap>
    </Sprite>
  );
}

// ── 第 8 幕：终幕（452–484s）─────────────────────────────────
function Scene8() {
  const t = useT();
  const cards = [
    { zh: '单子', c: MV.mnd, x: 330, body: '以上这一切结构的名字', at: 459.8 },
    { zh: '自函子', c: MV.fun, x: 750, body: 'F[_]：Option、Either、Future、IO…', at: 456.2 },
    { zh: '范畴', c: MV.cat, x: 1170, body: '自函子们组成的世界，箭头是自然变换', at: 457.4 },
    { zh: '幺半群', c: MV.mon, x: 1590, body: '乘法 flatten · 单位 pure · 结合律', at: 458.6 },
  ];
  const endFade = ev(t, 472, 1.2); // 收尾
  return (
    <Sprite start={452} end={484}>
      <FadeGroup from={452.5} to={473.5} exit={1.2}>
        <QuoteLine y={290} size={58} hlAt={[453.5, 453.9, 454.3, 454.7]} />
        {cards.map((d, i) => {
          const p = ev(t, d.at, 0.7, Easing.easeOutCubic);
          return (
            <div key={i} style={{
              position: 'absolute', left: d.x, top: 620, transform: `translate(-50%,-50%) translateY(${(1 - p) * 24}px)`,
              width: 384, minHeight: 200, opacity: p,
              border: `2px solid ${d.c}66`, borderRadius: 20, background: '#12171f',
              padding: '30px 32px', boxShadow: `0 0 30px ${d.c}1a`,
            }}>
              <div style={{ fontFamily: MV.serif, fontSize: 34, fontWeight: 700, color: d.c, marginBottom: 16 }}>{d.zh}</div>
              <div style={{ fontFamily: MV.sans, fontSize: 24, lineHeight: 1.6, color: '#c3cddb', textWrap: 'pretty' }}>{d.body}</div>
            </div>
          );
        })}
      </FadeGroup>

      {/* 收束语 */}
      <FadeGroup from={474} to={483.6} exit={1.4}>
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 460, textAlign: 'center',
          fontFamily: MV.serif, fontSize: 52, fontWeight: 700, color: MV.ink, letterSpacing: '0.06em',
          opacity: ev(t, 474.5, 1.0),
        }}>
          它从来不是魔法。
        </div>
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 560, textAlign: 'center',
          fontFamily: MV.serif, fontSize: 36, fontWeight: 600, color: MV.dim, letterSpacing: '0.08em',
          opacity: ev(t, 477, 1.0),
        }}>
          只是<span style={{ color: MV.cat }}>复合</span>，加上一点点<span style={{ color: MV.mon }}>纪律</span>。
        </div>
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 700, textAlign: 'center',
          fontFamily: MV.mono, fontSize: 22, color: MV.faint, letterSpacing: '0.3em',
          opacity: ev(t, 479.5, 1.0),
        }}>FIN</div>
      </FadeGroup>

      <Cap from={453} to={461.5}>现在再读这句话——每个词，都已经是你的旧相识。</Cap>
      <Cap from={461.5} to={471.5}>一个<Em c={MV.mnd}>单子</Em>，就是<Em c={MV.fun}>自函子</Em><Em c={MV.cat}>范畴</Em>上的一个<Em c={MV.mon}>幺半群</Em>。如今，这只是一句平凡的陈述。</Cap>
    </Sprite>
  );
}

Object.assign(window, { Scene7, Scene8 });
