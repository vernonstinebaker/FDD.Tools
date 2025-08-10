package net.sourceforge.fddtools.util;

import javafx.application.Platform;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Menu;
import net.sourceforge.fddtools.state.ModelEventBus;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central registry of UI Labeled nodes whose text should update automatically
 * when a UI language change event occurs. Components should call register()
 * after constructing the control and setting its i18n key via register(control, key).
 */
public final class I18nRegistry {
    private static final class LabeledEntry { final WeakReference<Labeled> ref; final String key; LabeledEntry(Labeled l,String k){ this.ref=new WeakReference<>(l); this.key=k; } }
    private static final class MenuEntry { final WeakReference<MenuItem> ref; final String key; MenuEntry(MenuItem i,String k){ this.ref=new WeakReference<>(i); this.key=k; } }
    private static final List<LabeledEntry> labeledEntries = new CopyOnWriteArrayList<>();
    private static final List<MenuEntry> menuEntries = new CopyOnWriteArrayList<>();

    static { ModelEventBus.get().subscribe(ev -> { if (ev.type == ModelEventBus.EventType.UI_LANGUAGE_CHANGED) refreshAll(); }); }
    private I18nRegistry() {}

    public static void register(Labeled labeled, String key) { if (labeled==null||key==null) return; labeledEntries.add(new LabeledEntry(labeled,key)); }
    public static void register(MenuItem item, String key) { if (item==null||key==null) return; menuEntries.add(new MenuEntry(item,key)); }
    public static void registerMenu(Menu menu, String key) { register((MenuItem)menu, key); }

    public static void refreshAll() {
        Runnable r = () -> {
            labeledEntries.removeIf(e -> { Labeled l = e.ref.get(); if(l==null) return true; try { l.setText(I18n.get(e.key)); } catch(Exception ignored){} return false; });
            menuEntries.removeIf(e -> { MenuItem m = e.ref.get(); if(m==null) return true; try { m.setText(I18n.get(e.key)); } catch(Exception ignored){} return false; });
        };
        if (Platform.isFxApplicationThread()) r.run(); else Platform.runLater(r);
    }
}
