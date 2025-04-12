package x.entt.XDragons.util;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

public class Messenger {
   public static String color(String msg) {
      return ChatColor.translateAlternateColorCodes('&', msg);
   }

   public static List<String> colorList(List<String> messages) {
      return messages.stream()
              .map(Messenger::color)
              .collect(Collectors.toList());
   }
}