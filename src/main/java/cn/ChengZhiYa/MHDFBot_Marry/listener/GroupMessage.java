package cn.ChengZhiYa.MHDFBot_Marry.listener;

import cn.ChengZhiYa.MHDFBot.api.MHDFBot;
import cn.ChengZhiYa.MHDFBot.api.builder.MessageBuilder;
import cn.ChengZhiYa.MHDFBot.api.interfaces.EventHandler;
import cn.ChengZhiYa.MHDFBot.api.manager.Listener;
import cn.ChengZhiYa.MHDFBot.entity.user.Member;
import cn.ChengZhiYa.MHDFBot.event.message.GroupMessageEvent;
import cn.ChengZhiYa.MHDFBot_Marry.entity.Marry;
import cn.ChengZhiYa.MHDFBot_Marry.main;
import cn.ChengZhiYa.MHDFBot_Marry.util.DatabaseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.ChengZhiYa.MHDFBot_Marry.util.LangUtil.i18n;

public final class GroupMessage implements Listener {
    private boolean chance(double chance) {
        return Math.random() * 100.0 < chance;
    }

    private List<Long> getMemberList(Long group) {
        List<Long> memberList = new ArrayList<>();
        for (Member member : MHDFBot.getGroupMemberList(group)) {
            memberList.add(member.getId());
        }
        return memberList;
    }

    @EventHandler
    public void onGroupMessage(GroupMessageEvent event) {
        if (main.instance.getConfig().getStringList("AllowUseGroup").contains(String.valueOf(event.getGroupId()))) {
            if (event.getMessage().startsWith("#")) {
                String[] args = event.getMessage().split(" ");
                MessageBuilder messageBuilder = MessageBuilder.builder();
                if (main.instance.getConfig().getBoolean("MessageSettings.Reply")) {
                    messageBuilder.reply(event.getMessageId());
                }
                if (main.instance.getConfig().getBoolean("MessageSettings.At")) {
                    messageBuilder.at(event.getSender().getUserId()).text("\n");
                }
                switch (args[0]) {
                    case "#群老婆" -> {
                        Marry marry = DatabaseUtil.getMarry(event.getGroupId(), event.getSender().getUserId());
                        if (marry == null) {
                            if (main.instance.getConfig().getBoolean("MarrySettings.Dog") && chance(main.instance.getConfig().getDouble("MarrySettings.DogChange"))) {
                                DatabaseUtil.setRole(event.getSender().getUserId(), "Dog");
                            }
                            if (main.instance.getConfig().getBoolean("MarrySettings.OptionalWife") && chance(main.instance.getConfig().getDouble("MarrySettings.OptionalWifeChange"))) {
                                DatabaseUtil.setRole(event.getSender().getUserId(), "OptionalWife");
                            }
                            String role = DatabaseUtil.getRole(event.getSender().getUserId());
                            if (role == null) {
                                List<Long> allowMarryList = getMemberList(event.getGroupId());
                                allowMarryList.removeAll(DatabaseUtil.getMarryList(event.getGroupId()));

                                Random random = new Random();
                                Long mrs = allowMarryList.get(random.nextInt(allowMarryList.size()));

                                DatabaseUtil.setMarry(event.getGroupId(), new Marry(event.getSender().getUserId(), mrs));
                                messageBuilder.text(i18n("Marry.IsMr").replaceAll("\\{AtMrs}", MessageBuilder.builder().at(mrs).build()));
                            } else {
                                if (role.equals("Dog")) {
                                    messageBuilder.text(i18n("Marry.IsDog"));
                                }
                                if (role.equals("OptionalWife")) {
                                    messageBuilder.text(i18n("Marry.CanOptionalWife"));
                                }
                            }
                        } else {
                            if (Objects.equals(marry.getMr(), event.getSender().getUserId())) {
                                messageBuilder.text(i18n("Marry.IsMr").replaceAll("\\{AtMrs}", MessageBuilder.builder().at(marry.getMrs()).build()));
                            } else {
                                messageBuilder.text(i18n("Marry.IsMrs").replaceAll("\\{AtMr}", MessageBuilder.builder().at(marry.getMrs()).build()));
                            }
                        }
                        MHDFBot.sendMessage(event, messageBuilder.build());
                    }
                    case "#选老婆" -> {
                        if (main.instance.getConfig().getBoolean("MarrySettings.OptionalWife")) {
                            String role = DatabaseUtil.getRole(event.getSender().getUserId());
                            Marry marry = DatabaseUtil.getMarry(event.getGroupId(), event.getSender().getUserId());
                            if (role != null && role.equals("OptionalWife")) {
                                if (marry == null) {
                                    if (args.length == 2) {
                                        Pattern pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)(?:,name=[^]]+)?]");
                                        Matcher matcher = pattern.matcher(event.getMessage());
                                        if (matcher.find()) {
                                            Long mrs = Long.parseLong(matcher.group(1));
                                            if (DatabaseUtil.getMarry(event.getGroupId(), mrs) == null) {
                                                DatabaseUtil.setMarry(event.getGroupId(), new Marry(event.getSender().getUserId(), mrs));
                                                messageBuilder.text(i18n("Marry.IsMr").replaceAll("\\{AtMrs}", MessageBuilder.builder().at(mrs).build()));
                                            } else {
                                                messageBuilder.text(i18n("Marry.HaveMarry"));
                                            }
                                        } else {
                                            messageBuilder.text(i18n("Marry.OptionalWife"));
                                        }
                                    } else {
                                        messageBuilder.text(i18n("Marry.OptionalWife"));
                                    }
                                } else {
                                    messageBuilder.text(i18n("Marry.HavaWife"));
                                }
                            } else {
                                messageBuilder.text(i18n("Marry.CantOptionalWife"));
                            }
                            MHDFBot.sendMessage(event, messageBuilder.build());
                        }
                    }
                    case "#换老婆" -> {
                        if (main.instance.getConfig().getBoolean("MarrySettings.ChangeWife")) {
                            Marry marry = DatabaseUtil.getMarry(event.getGroupId(), event.getSender().getUserId());
                            if (marry != null && Objects.equals(marry.getMr(), event.getSender().getUserId())) {
                                if (DatabaseUtil.getChangeWifeTimesHashMap(event.getGroupId(), event.getSender().getUserId()) < main.instance.getConfig().getInt("MarrySettings.MaxChangeTimes")) {
                                    List<Long> allowMarryList = getMemberList(event.getGroupId());
                                    allowMarryList.removeAll(DatabaseUtil.getMarryList(event.getGroupId()));

                                    Random random = new Random();
                                    Long mrs = allowMarryList.get(random.nextInt(allowMarryList.size()));

                                    marry.setMrs(mrs);
                                    DatabaseUtil.changeMarry(event.getGroupId(), marry);
                                    DatabaseUtil.addChangeWifeTimes(event.getGroupId(), event.getSender().getUserId());
                                    messageBuilder.text(i18n("Marry.IsMr").replaceAll("\\{AtMrs}", MessageBuilder.builder().at(mrs).build()));
                                } else {
                                    messageBuilder.text(i18n("Marry.ChangeWifeMax"));
                                }
                            } else {
                                messageBuilder.text(i18n("Marry.NotHaveWife"));
                            }
                            MHDFBot.sendMessage(event, messageBuilder.build());
                        }
                    }
                }
            }
        }
    }
}
