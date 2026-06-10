// fp-main.jsx — assemblage du film + chapitrage cliquable

const SCENES = {
  intro: SceneIntro, functor: SceneFunctor, applic: SceneApplic, monad: SceneMonad,
  either: SceneEither, validated: SceneValidated, traverse: SceneTraverse, state: SceneState,
  kleisli: SceneKleisli, readert: SceneReaderT, funk: SceneFunK,
  tagless: SceneTagless, free: SceneFree, yoneda: SceneYoneda, finale: SceneFinale,
};

const RAIL_LABELS = {
  intro: 'Intro', functor: 'Functor', applic: 'Applicative', monad: 'Monad',
  either: 'Either', validated: 'Validated', traverse: 'Traverse', state: 'State',
  kleisli: 'Kleisli', readert: 'ReaderT', funk: 'F ~> G',
  tagless: 'Tagless', free: 'Free', yoneda: 'Yoneda', finale: 'Carte',
};

// Barre de chapitres (hors du canvas mis à l'échelle, via portal)
function ChapterRail() {
  const { time, setTime, setPlaying } = useTimeline();
  const host = React.useMemo(() => {
    const el = document.createElement('div');
    document.body.appendChild(el);
    return el;
  }, []);
  React.useEffect(() => () => { host.remove(); }, [host]);

  const current = CHAPTERS.find((c) => time >= c.start && time < c.end) || CHAPTERS[0];

  return ReactDOM.createPortal(
    <div data-rail-active={current.id} style={{
      position: 'fixed', top: 0, left: 0, right: 0, zIndex: 50,
      display: 'flex', justifyContent: 'center', gap: 4, padding: '8px 12px',
      background: 'rgba(16,16,15,0.92)', borderBottom: '1px solid rgba(255,255,255,0.08)',
      overflowX: 'auto', flexWrap: 'nowrap',
    }}>
      {CHAPTERS.map((c) => {
        const active = c.id === current.id;
        return (
          <button key={c.id}
            onClick={() => { setTime(c.start + 0.01); setPlaying(true); }}
            title={`${c.title} — ${c.sub || ''}`}
            style={{
              flexShrink: 0, display: 'flex', alignItems: 'center', gap: 7,
              padding: '5px 11px', borderRadius: 6, cursor: 'pointer',
              border: `1px solid ${active ? c.color : 'rgba(255,255,255,0.12)'}`,
              background: active ? `color-mix(in oklab, ${c.color} 22%, transparent)` : 'transparent',
              color: active ? '#fff' : 'rgba(246,244,239,0.6)',
              fontFamily: "'IBM Plex Mono', monospace", fontSize: 12, fontWeight: active ? 600 : 400,
              whiteSpace: 'nowrap', transition: 'all 150ms',
            }}>
            {c.num && <span style={{ color: active ? c.color : 'rgba(246,244,239,0.35)', fontSize: 10 }}>{c.num}</span>}
            {RAIL_LABELS[c.id]}
          </button>
        );
      })}
    </div>,
    host
  );
}

function Film() {
  const { time, setTime, setPlaying } = useTimeline();
  React.useEffect(() => {
    window.__seek = (t, play = false) => { setTime(t); setPlaying(play); };
  }, [setTime, setPlaying]);
  const current = CHAPTERS.find((c) => time >= c.start && time < c.end) || CHAPTERS[0];
  const sec = Math.floor(time);
  const label = `t=${Math.floor(sec / 60)}:${String(sec % 60).padStart(2, '0')} · ${current.title}`;
  return (
    <div style={{ position: 'absolute', inset: 0 }} data-screen-label={label}>
      <Paper></Paper>
      {CHAPTERS.map((c) => {
        const S = SCENES[c.id];
        return (
          <Sprite key={c.id} start={c.start} end={c.end}>
            <S></S>
          </Sprite>
        );
      })}
      <ChapterRail></ChapterRail>
    </div>
  );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <Stage width={1920} height={1080} duration={TOTAL_DUR} background={PAPER} persistKey="fp-scala3" loop={false}>
    <Film></Film>
  </Stage>
);
