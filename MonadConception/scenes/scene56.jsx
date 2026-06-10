// scene56.jsx — 第 5 幕：幺半群（218–290s）；第 6 幕：自函子范畴上的幺半群（290–388s）

// 方块 tile
function Tile({ x, y, label, color = MV.mon, w = 130, h = 110, fontSize = 38, opacity = 1, scale = 1, glow = 0 }) {
  return (
    <div style={{
      position: 'absolute', left: x, top: y, width: w, height: h,
      transform: `translate(-50%,-50%) scale(${scale})`,
      borderRadius: 18, border: `2.5px solid ${color}`, background: `${color}16`,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: MV.mono, fontSize, fontWeight: 700, color: MV.ink,
      opacity, boxShadow: `0 0 ${18 + glow * 36}px ${color}${glow > 0.5 ? '66' : '2a'}`,
      whiteSpace: 'nowrap',
    }}>{label}</div>
  );
}

// 两元素合并演示：a ⊕ b → r（unitSide: 'a' 表示 a 是单位元，合并时淡出强调“没起作用”）
function MergeDemo({ from, cx, y, a, b, r, color = MV.mon, w = 150, fontSize = 40, gap = 230 }) {
  const t = useT();
  const lt = t - from;
  if (lt < 0) return null;
  const slide = animate({ from: 0, to: 1, start: 1.0, end: 1.9 })(lt);
  const fuse = animate({ from: 0, to: 1, start: 1.9, end: 2.45, ease: Easing.easeOutBack })(lt);
  const ax = cx - gap / 2 + slide * (gap / 2 - w * 0.28);
  const bx = cx + gap / 2 - slide * (gap / 2 - w * 0.28);
  const inO = ev(t, from, 0.5);
  return (
    <div>
      <Tile x={ax} y={y} label={a} color={color} w={w} fontSize={fontSize} opacity={inO * (1 - fuse)} />
      <Tile x={bx} y={y} label={b} color={color} w={w} fontSize={fontSize} opacity={inO * (1 - fuse)} />
      <div style={{
        position: 'absolute', left: cx, top: y, transform: 'translate(-50%,-50%)',
        fontFamily: MV.mono, fontSize: 34, color: MV.dim, opacity: inO * (1 - slide),
      }}>⊕</div>
      {fuse > 0 ? <Tile x={cx} y={y} label={r} color={color} w={w * 1.3} fontSize={fontSize} scale={0.7 + 0.3 * fuse} opacity={fuse} glow={fuse} /> : null}
    </div>
  );
}

// 结合律一列：((a·b)·c) 或 (a·(b·c))
function AssocCol({ from, cx, y, firstPair }) {
  const t = useT();
  const lt = t - from;
  if (lt < 0) return null;
  const w = 110, gap = 170;
  const xs = [cx - gap, cx, cx + gap];
  const labels = ['a', 'b', 'c'];
  const inO = ev(t, from, 0.45);
  // 阶段1：firstPair 两块合并；阶段2：与第三块合并
  const s1 = animate({ from: 0, to: 1, start: 0.9, end: 1.7 })(lt);
  const f1 = animate({ from: 0, to: 1, start: 1.7, end: 2.1, ease: Easing.easeOutBack })(lt);
  const s2 = animate({ from: 0, to: 1, start: 2.7, end: 3.5 })(lt);
  const f2 = animate({ from: 0, to: 1, start: 3.5, end: 3.95, ease: Easing.easeOutBack })(lt);
  const pi = firstPair === 'ab' ? [0, 1] : [1, 2];
  const oi = firstPair === 'ab' ? 2 : 0;
  const pairCx = (xs[pi[0]] + xs[pi[1]]) / 2;
  const pairLabel = firstPair === 'ab' ? 'a·b' : 'b·c';
  // 阶段2位置
  const fusedX = pairCx + s2 * (cx - pairCx);
  const otherX = xs[oi] + s2 * (cx - xs[oi]);
  return (
    <div>
      {pi.map((i) => {
        const x = xs[i] + s1 * (pairCx - xs[i]);
        return <Tile key={i} x={x} y={y} label={labels[i]} w={w} h={96} fontSize={34} opacity={inO * (1 - f1)} />;
      })}
      {f1 > 0 && f2 < 1 ? <Tile x={fusedX} y={y} label={pairLabel} w={150} h={96} fontSize={32} opacity={f1 * (1 - f2)} scale={0.75 + 0.25 * f1} /> : null}
      <Tile x={otherX} y={y} label={labels[oi]} w={w} h={96} fontSize={34} opacity={inO * (1 - f2)} />
      {f2 > 0 ? <Tile x={cx} y={y} label="a·b·c" w={190} h={100} fontSize={32} opacity={f2} scale={0.75 + 0.25 * f2} glow={f2} /> : null}
      <div style={{
        position: 'absolute', left: cx, top: y - 110, transform: 'translateX(-50%)',
        fontFamily: MV.mono, fontSize: 27, color: MV.dim, opacity: inO, whiteSpace: 'nowrap',
      }}>{firstPair === 'ab' ? '(a ⊕ b) ⊕ c' : 'a ⊕ (b ⊕ c)'}</div>
    </div>
  );
}

// ── 第 5 幕：幺半群（218–290s）───────────────────────────────
function Scene5() {
  const t = useT();
  return (
    <Sprite start={218} end={290}>
      <Chapter from={218.5} to={289} num="Chapter 04" zh="幺半群" en="Monoid" color={MV.mon} />

      {/* A：整数加法 */}
      <FadeGroup from={219} to={240.5} exit={0.6}>
        <MergeDemo from={220} cx={960} y={420} a="1" b="2" r="3" />
        <div style={{
          position: 'absolute', left: 960, top: 560, transform: 'translateX(-50%)',
          fontFamily: MV.mono, fontSize: 28, color: MV.dim, opacity: ev(t, 223.5, 0.6), whiteSpace: 'nowrap',
        }}>(Int, +)</div>
        {/* 单位元 0 */}
        <MergeDemo from={229.5} cx={960} y={680} a="0" b="5" r="5" />
        <div style={{
          position: 'absolute', left: 960, top: 800, transform: 'translateX(-50%)',
          fontFamily: MV.sans, fontSize: 26, color: MV.dim, opacity: ev(t, 233, 0.6), whiteSpace: 'nowrap',
        }}>0 是<span style={{ color: MV.mon }}>单位元</span>：加了等于没加</div>
      </FadeGroup>

      {/* B：字符串 / 列表 */}
      <FadeGroup from={241} to={252.5} exit={0.6}>
        <MergeDemo from={241.5} cx={960} y={430} a={'"flat"'} b={'"Map"'} r={'"flatMap"'} w={230} fontSize={34} gap={400} />
        <div style={{
          position: 'absolute', left: 960, top: 580, transform: 'translateX(-50%)',
          fontFamily: MV.mono, fontSize: 28, color: MV.dim, opacity: ev(t, 245, 0.6), whiteSpace: 'nowrap',
        }}>(String, ++)　单位元 ""</div>
        <div style={{
          position: 'absolute', left: 960, top: 680, transform: 'translateX(-50%)',
          fontFamily: MV.mono, fontSize: 30, color: MV.ink, opacity: ev(t, 247.5, 0.7), whiteSpace: 'nowrap',
        }}>
          List(1, 2) <span style={{ color: MV.mon }}>++</span> List(3) <span style={{ color: MV.faint }}>==</span> List(1, 2, 3)
          <span style={{ color: MV.dim, fontSize: 25 }}>　单位元 Nil</span>
        </div>
      </FadeGroup>

      {/* C：结合律 */}
      <FadeGroup from={253} to={266.5} exit={0.6}>
        <AssocCol from={254} cx={560} y={500} firstPair="ab" />
        <AssocCol from={254} cx={1360} y={500} firstPair="bc" />
        <div style={{
          position: 'absolute', left: 960, top: 500, transform: 'translate(-50%,-50%)',
          fontFamily: MV.mono, fontSize: 52, fontWeight: 700, color: MV.mon,
          opacity: ev(t, 258.4, 0.6),
        }}>==</div>
        <div style={{
          position: 'absolute', left: 960, top: 680, transform: 'translateX(-50%)', textAlign: 'center',
          fontFamily: MV.sans, fontSize: 27, color: MV.dim, opacity: ev(t, 260, 0.8), whiteSpace: 'nowrap',
        }}>唯一的要求：<span style={{ color: MV.mon }}>结合律</span>。先算哪一边，结果都一样。</div>
      </FadeGroup>

      {/* D：抽象出形状 */}
      <FadeGroup from={267} to={289.2} exit={0.8}>
        {(() => {
          const lift = ev(t, 280.5, 1.2);
          const glow = lift;
          return (
            <div style={{
              position: 'absolute', left: 960, top: 530 - lift * 30, transform: 'translate(-50%,-50%)',
              width: 880, borderRadius: 24,
              border: `2.5px solid ${MV.mon}${glow > 0.3 ? 'cc' : '66'}`,
              background: '#131820',
              boxShadow: `0 0 ${24 + glow * 70}px ${MV.mon}${glow > 0.3 ? '44' : '1a'}`,
              padding: '44px 64px',
            }}>
              <div style={{ fontFamily: MV.serif, fontSize: 34, fontWeight: 700, color: MV.mon, marginBottom: 30, textAlign: 'center', letterSpacing: '0.08em' }}>
                幺半群的「形状」
              </div>
              <div style={{ fontFamily: MV.mono, fontSize: 31, lineHeight: 2.0, color: MV.ink }}>
                <div style={{ opacity: ev(t, 268.5, 0.6) }}>
                  <span style={{ color: MV.mon }}>combine</span><span style={{ color: MV.faint }}> : </span>(M, M) =&gt; M
                  <span style={{ color: MV.faint, fontSize: 25 }}>　// 乘法</span>
                </div>
                <div style={{ opacity: ev(t, 270.5, 0.6) }}>
                  <span style={{ color: MV.mon }}>empty</span><span style={{ color: MV.faint }}>   : </span>M
                  <span style={{ color: MV.faint, fontSize: 25 }}>　　　　 // 单位元</span>
                </div>
                <div style={{ opacity: ev(t, 272.5, 0.6), fontSize: 27, color: MV.dim, marginTop: 8 }}>
                  约束：combine 满足结合律，empty 两边都不起作用
                </div>
              </div>
            </div>
          );
        })()}
      </FadeGroup>

      <Cap from={219.5} to={228.8}>现在暂时离开范畴，看一个简单得多的结构。两个东西，<Em>合并</Em>成一个。</Cap>
      <Cap from={228.8} to={240.6}>一堆东西、一个二元运算、一个<Em>不起作用的单位元</Em>——这就是幺半群。</Cap>
      <Cap from={240.6} to={252.6}>字符串拼接、列表拼接……换了衣服，还是<Em>同一个模式</Em>。</Cap>
      <Cap from={252.6} to={266.6}>它只提一个要求：<Em>结合律</Em>。括号加在哪儿都行——这让“任意分组归并”成为可能。</Cap>
      <Cap from={266.6} to={279.8}>把具体例子抹掉，只剩一个形状：一个<Em>乘法</Em>，一个<Em>单位</Em>，一条结合律。</Cap>
      <Cap from={279.8} to={289.4}>关键一步来了——这个形状<Em c={MV.mnd}>并不挑剔 M 是什么</Em>。哪个世界里有乘法和单位，哪个世界里就有幺半群。</Cap>
    </Sprite>
  );
}

// ── 第 6 幕：自函子范畴上的幺半群（290–388s）─────────────────
function Scene6() {
  const t = useT();
  const O = { x: 640, y: 420 }, L = { x: 1240, y: 380 }, FU = { x: 940, y: 660 };
  const p2 = 326, p3 = 358, p4 = 374; // 阶段起点：乘法 / 定律 / 集大成
  return (
    <Sprite start={290} end={388}>
      <Chapter from={290.5} to={387} num="Chapter 05" zh="自函子范畴上的幺半群" en="" color={MV.mnd} />

      {/* P1：自函子们自己的范畴 */}
      <FadeGroup from={291} to={p2 - 0.5} exit={0.8}>
        <SvgLayer>
          <Arrow x1={L.x} y1={L.y} x2={O.x} y2={O.y} bend={-70} color={MV.pur} width={4.5} from={303} dur={1.0}
            label="headOption" labelDy={-26} shrink={92} />
          <Arrow x1={O.x} y1={O.y} x2={L.x} y2={L.y} bend={-70} color={MV.pur} width={3} from={306} dur={1.0}
            label="toList" labelDy={48} shrink={92} opacity={0.6} />
          <Arrow x1={FU.x - 40} y1={FU.y - 30} x2={O.x + 30} y2={O.y + 50} bend={20} color={MV.pur} width={3} from={307.5} dur={0.9}
            shrink={70} opacity={0.45} />
        </SvgLayer>
        {[{ n: O, l: 'Option' }, { n: L, l: 'List' }, { n: FU, l: 'Future' }].map((d, i) => (
          <div key={i} style={{
            position: 'absolute', left: d.n.x, top: d.n.y,
            transform: `translate(-50%,-50%) scale(${0.7 + 0.3 * ev(t, 292.5 + i * 0.6, 0.6, Easing.easeOutBack)})`,
            opacity: Math.min(1, ev(t, 292.5 + i * 0.6, 0.6) * 1.3),
            width: 168, height: 168, borderRadius: '50%',
            border: `3px solid ${MV.fun}`, background: `${MV.fun}14`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontFamily: MV.mono, fontSize: 30, fontWeight: 700, color: MV.fun,
            boxShadow: `0 0 36px ${MV.fun}2e`,
          }}>{d.l}</div>
        ))}
        {/* 自然变换示例 */}
        <FadeGroup from={308.5} to={p2 - 0.5} enter={0.6}>
          <div style={{
            position: 'absolute', left: 960, top: 855, transform: 'translateX(-50%)',
            fontFamily: MV.mono, fontSize: 29, color: MV.ink, whiteSpace: 'nowrap',
          }}>
            List(1, 2, 3)<span style={{ color: MV.pur }}>.headOption</span>
            <span style={{ color: MV.faint }}>  ⇒  </span>Some(1)
            <span style={{ color: MV.dim, fontFamily: MV.sans, fontSize: 24 }}>　换容器，不碰值</span>
          </div>
        </FadeGroup>
        <div style={{
          position: 'absolute', left: 960, top: 188, transform: 'translateX(-50%)',
          fontFamily: MV.sans, fontSize: 25, color: MV.dim, letterSpacing: '0.1em',
          opacity: ev(t, 296, 0.8), whiteSpace: 'nowrap',
        }}>对象 = <span style={{ color: MV.fun }}>自函子</span>　·　箭头 = <span style={{ color: MV.pur }}>自然变换</span></div>
      </FadeGroup>

      {/* 右上角任务卡：乘法=? 单位=?（贯穿 P1 末尾到 P3）*/}
      <FadeGroup from={315} to={p4 - 1} exit={0.6}>
        <div style={{
          position: 'absolute', right: 70, top: 170, width: 470,
          border: `2px solid ${MV.mon}77`, borderRadius: 20, background: '#131820',
          padding: '26px 34px', boxShadow: `0 0 30px ${MV.mon}1a`,
        }}>
          <div style={{ fontFamily: MV.serif, fontSize: 26, fontWeight: 700, color: MV.mon, marginBottom: 18 }}>套用幺半群形状</div>
          {[
            { q: '乘法 combine', a: 'flatten', at: 339 },
            { q: '单位 empty', a: 'pure', at: 353 },
          ].map((d, i) => {
            const ans = ev(t, d.at, 0.5, Easing.easeOutBack);
            return (
              <div key={i} style={{ fontFamily: MV.mono, fontSize: 24, color: MV.ink, display: 'flex', alignItems: 'center', gap: 12, minHeight: 48, opacity: ev(t, 316 + i * 1.2, 0.5), whiteSpace: 'nowrap' }}>
                <span style={{ color: MV.dim }}>{d.q} = </span>
                {ans > 0 ? (
                  <span style={{ color: MV.mnd, fontWeight: 700, transform: `scale(${0.6 + 0.4 * ans})`, display: 'inline-block', opacity: ans }}>{d.a} ✓</span>
                ) : (
                  <span style={{ color: MV.faint }}>?</span>
                )}
              </div>
            );
          })}
        </div>
      </FadeGroup>

      {/* P2：乘法 = 复合 + flatten；单位 = Id + pure */}
      <FadeGroup from={p2} to={p3 - 0.5} exit={0.7}>
        {/* 乘法行 */}
        <div style={{
          position: 'absolute', left: 120, top: 268, fontFamily: MV.sans, fontSize: 26, color: MV.dim,
          opacity: ev(t, p2 + 0.5, 0.6), whiteSpace: 'nowrap',
        }}>自函子的「乘法」：把 F 套进 F —— <span style={{ color: MV.fun, fontFamily: MV.mono }}>F ∘ F</span></div>
        <BoxC x={150} y={330} w={460} h={250} color={MV.fun} label="Some" from={p2 + 1.5}>
          <BoxC x={110} y={56} w={240} h={150} color={MV.fun} label="Some" from={p2 + 2.1} labelSize={19} style={{ position: 'absolute' }}>
            <ValDot x={120} y={78} label="42" from={p2 + 2.6} size={64} style={{ position: 'absolute' }} />
          </BoxC>
        </BoxC>
        <SvgLayer>
          <Arrow x1={660} y1={455} x2={980} y2={455} color={MV.mnd} width={5} from={p2 + 5} dur={0.9}
            label="flatten（μ）" labelDy={-24} labelSize={27} shrink={14} />
          <Arrow x1={640} y1={800} x2={980} y2={800} color={MV.mnd} width={5} from={p2 + 19} dur={0.9}
            label="pure（η）" labelDy={-24} labelSize={27} shrink={14} />
        </SvgLayer>
        <BoxC x={1020} y={372} w={300} h={170} color={MV.fun} label="Some" from={p2 + 5.9}>
          <ValDot x={150} y={86} label="42" from={p2 + 6.3} size={64} style={{ position: 'absolute' }} />
        </BoxC>
        <div style={{
          position: 'absolute', left: 120, top: 615, fontFamily: MV.mono, fontSize: 28, color: MV.ink,
          opacity: ev(t, p2 + 7, 0.7), whiteSpace: 'nowrap',
        }}>
          <span style={{ color: MV.mon }}>combine</span> : F ∘ F ~&gt; F
          <span style={{ color: MV.faint }}>　即　</span>
          <span style={{ color: MV.mnd }}>F[F[A]] =&gt; F[A]</span>
        </div>
        {/* 单位行 */}
        <ValDot x={520} y={800} label="42" from={p2 + 17.5} size={80} fontSize={30} />
        <div style={{
          position: 'absolute', left: 380, top: 855, fontFamily: MV.sans, fontSize: 23, color: MV.dim,
          opacity: ev(t, p2 + 17.8, 0.6), width: 280, textAlign: 'center', marginLeft: -0,
        }}>裸值 · Id[A] = A</div>
        <BoxC x={1020} y={718} w={300} h={164} color={MV.fun} label="Some" from={p2 + 20.2}>
          <ValDot x={150} y={84} label="42" from={p2 + 20.6} size={64} style={{ position: 'absolute' }} />
        </BoxC>
        <div style={{
          position: 'absolute', left: 1390, top: 752, fontFamily: MV.mono, fontSize: 27, color: MV.ink,
          opacity: ev(t, p2 + 21.5, 0.7), whiteSpace: 'nowrap', lineHeight: 1.7,
        }}>
          <span style={{ color: MV.mon }}>empty</span> : Id ~&gt; F<br />
          <span style={{ color: MV.faint }}>即 </span><span style={{ color: MV.mnd }}>A =&gt; F[A]</span>
        </div>
      </FadeGroup>

      {/* P3：两条定律 */}
      <FadeGroup from={p3} to={p4 - 0.5} exit={0.6}>
        <div style={{ position: 'absolute', left: 0, right: 0, top: 250, textAlign: 'center', fontFamily: MV.serif, fontSize: 33, fontWeight: 700, color: MV.ink, opacity: ev(t, p3 + 0.3, 0.6) }}>
          幺半群定律，自动翻译成<span style={{ color: MV.mnd }}>单子定律</span>
        </div>
        <SvgLayer>
          {/* 结合律菱形 */}
          <Arrow x1={490} y1={430} x2={930} y2={430} color={MV.dim} width={3.5} from={p3 + 2.2} dur={0.7} label="flatten" labelDy={-16} labelSize={22} shrink={110} />
          <Arrow x1={930} y1={430} x2={1370} y2={430} color={MV.dim} width={3.5} from={p3 + 3.0} dur={0.7} label="flatten" labelDy={-16} labelSize={22} shrink={80} />
          <Arrow x1={490} y1={470} x2={930} y2={580} color={MV.dim} width={3.5} from={p3 + 4.0} dur={0.7} label="map(flatten)" labelDy={36} labelSize={22} shrink={110} />
          <Arrow x1={930} y1={580} x2={1370} y2={470} color={MV.dim} width={3.5} from={p3 + 4.8} dur={0.7} label="flatten" labelDy={36} labelSize={22} shrink={80} />
        </SvgLayer>
        {[
          { x: 490, y: 430, l: 'F[F[F[A]]]', c: MV.fun, at: p3 + 1.5 },
          { x: 930, y: 430, l: 'F[F[A]]', c: MV.fun, at: p3 + 2.8 },
          { x: 930, y: 580, l: 'F[F[A]]', c: MV.fun, at: p3 + 4.6 },
          { x: 1370, y: 450, l: 'F[A]', c: MV.mnd, at: p3 + 5.6 },
        ].map((d, i) => (
          <div key={i} style={{
            position: 'absolute', left: d.x, top: d.y, transform: 'translate(-50%,-50%)',
            fontFamily: MV.mono, fontSize: 30, fontWeight: 700, color: d.c,
            opacity: ev(t, d.at, 0.5), background: MV.bg, padding: '4px 14px',
            border: `2px solid ${d.c}44`, borderRadius: 12,
          }}>{d.l}</div>
        ))}
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 700, textAlign: 'center',
          fontFamily: MV.mono, fontSize: 28, color: MV.ink, opacity: ev(t, p3 + 8.5, 0.7),
        }}>
          fa<span style={{ color: MV.mnd }}>.pure.flatten</span> <span style={{ color: MV.faint }}>==</span> fa <span style={{ color: MV.faint }}>==</span> fa<span style={{ color: MV.mnd }}>.map(pure).flatten</span>
          <span style={{ color: MV.dim, fontFamily: MV.sans, fontSize: 24 }}>　（单位律：pure 进去再压平，什么都没发生）</span>
        </div>
      </FadeGroup>

      {/* P4：集大成 */}
      <FadeGroup from={p4} to={387.4} exit={0.8}>
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 330, textAlign: 'center',
          fontFamily: MV.mono, fontSize: 48, fontWeight: 700, opacity: ev(t, p4 + 0.5, 0.8),
        }}>
          <span style={{ color: MV.mnd }}>Monad</span><span style={{ color: MV.faint }}> = ( </span>
          <span style={{ color: MV.fun }}>F[_]</span><span style={{ color: MV.faint }}>, </span>
          <span style={{ color: MV.mon }}>flatten</span><span style={{ color: MV.faint }}>, </span>
          <span style={{ color: MV.mon }}>pure</span><span style={{ color: MV.faint }}> )</span>
        </div>
        <div style={{
          position: 'absolute', left: 0, right: 0, top: 430, textAlign: 'center',
          fontFamily: MV.sans, fontSize: 26, color: MV.dim, opacity: ev(t, p4 + 2, 0.8),
        }}>外加结合律与单位律</div>
        <QuoteLine y={560} size={54} hlAt={[p4 + 4.0, p4 + 4.6, p4 + 5.2, p4 + 5.8]} />
      </FadeGroup>

      <Cap from={291.5} to={302.6}>既然自函子这么重要，干脆给它们盖一座<Em c={MV.cat}>自己的范畴</Em>：每一个自函子，是其中一个对象。</Cap>
      <Cap from={302.6} to={314.4}>对象之间的箭头叫<Em c={MV.pur}>自然变换</Em>：把一种容器整体换成另一种，而不碰里面的值。</Cap>
      <Cap from={314.4} to={p2 + 0.2}>现在，把第四章那个幺半群的形状<Em c={MV.mon}>套到这座范畴上</Em>。只需回答两个问题：乘法是什么？单位是什么？</Cap>
      <Cap from={p2 + 0.2} to={p2 + 12}>自函子的乘法就是<Em c={MV.fun}>复合</Em>：F 套 F，得到 F[F[A]]。而 combine: F∘F ~&gt; F，展开看就是 <Em c={MV.mnd}>F[F[A]] =&gt; F[A]</Em>。</Cap>
      <Cap from={p2 + 12} to={p2 + 17}>等一下——这不就是 <Em c={MV.mnd}>flatten</Em> 吗！第三章那个嵌套的不适感，在这里被一招化解。</Cap>
      <Cap from={p2 + 17} to={p3 - 0.3}>单位呢？是什么都不做的恒等函子 Id。empty: Id ~&gt; F，翻译过来就是 A =&gt; F[A]——正是 <Em c={MV.mnd}>pure</Em>。</Cap>
      <Cap from={p3 - 0.3} to={p3 + 8.2}><Em c={MV.mon}>结合律</Em>说：三层嵌套，先压哪两层都一样，殊途同归。</Cap>
      <Cap from={p3 + 8.2} to={p4 - 0.3}><Em c={MV.mon}>单位律</Em>说：pure 放进去再 flatten，等于什么都没发生。</Cap>
      <Cap from={p4 + 0.3} to={387.6}>谜底揭晓：在自函子的范畴里选一个 F，配上乘法 flatten 与单位 pure——这个<Em c={MV.mon}>幺半群</Em>，就叫<Em c={MV.mnd}>单子</Em>。</Cap>
    </Sprite>
  );
}

Object.assign(window, { Scene5, Scene6, Tile, MergeDemo, AssocCol });
